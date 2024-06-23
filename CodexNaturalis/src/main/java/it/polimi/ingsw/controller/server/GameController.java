package it.polimi.ingsw.controller.server;

import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.GameModel;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.NetworkHandler;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.*;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.GameColorsMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Logger;

/**
 * It contains the game logic and ensures the correct game flow.
 * Each game has one: it changes the status of the match model based on the requested player interaction.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class GameController implements Runnable{

    //the game's model.
    private final GameModel game;

    //the game's basic information.
    private final GameInfo gameInfo;

    //stores and notifies the networkHandlers.
    private final ServerSubject serverSubject;

    //stores the messages sent by the networkHandlers.
    private final Queue<LabeledMessage> messageQueue;

    //keeps track of which users have answered a ping.
    private final List<NetworkHandler> connectedUsers;

    //run when the game is canceled or the match ends.
    private final Consumer<GameController> endGameProcedure;

    //used to stop the game from continuing when only one player is present.
    private final Object onlyOnePlayerLock;

    //tells whether the game is over.
    private final AtomicBoolean gameOver;

    //logs information about the game.
    private final Logger logger;

    //used to send pings periodically and check for disconnected users.
    private Timer pingTimer;

    //stores which player has the current turn.
    private String playerWithTurn;

    //tells whether there's only one connected player.
    private boolean onlyOnePlayer;

    /**
     * Class constructor.
     *
     * @param numberOfPlayers         the maximum number of players that can join the game.
     * @param serverSubject           the object used to notify changes in the game's model
     *                                as well as requesting a player action.
     * @param gameInfo                the game's information.
     * @param endGameProcedure        the consumer used to delete the game controller when the game ends.
     *
     * @throws IllegalNumberOfPlayers if the player entered an invalid maximum number of players when creating the game.
     *
     * @see GameInfo
     * @see ServerSubject
     */
    public GameController(int numberOfPlayers,
                          ServerSubject serverSubject,
                          GameInfo gameInfo,
                          Consumer<GameController> endGameProcedure) throws IllegalNumberOfPlayers {
        this.game = new GameModel(numberOfPlayers, serverSubject, gameInfo.getGameId());
        this.gameInfo = gameInfo;
        this.serverSubject = serverSubject;
        this.messageQueue = new LinkedList<>();
        this.connectedUsers = new ArrayList<>();
        this.endGameProcedure = endGameProcedure;
        this.onlyOnePlayerLock = new Object();
        this.onlyOnePlayer = false;
        this.gameOver = new AtomicBoolean(false);
        this.pingTimer = null;
        this.playerWithTurn = "";
        this.logger = Logger.getLogger(Parameters.getLoggerName());
    }

    /**
     * Adds a player to the lobby, if the given nickname and color are valid.
     *
     * @param nickname the nickname of the player.
     * @param color    the color of the player.
     * @param handler  the TCP/RMI handler associated to the new player.
     *
     * @see NetworkHandler
     */
    private void acceptPlayer(String nickname, Content color, NetworkHandler handler){
        try {
            game.addPlayerData(nickname, color, handler);
            handler.setCurrentGame(this);
            receivePing(handler);
        }catch(GameFullException G){
            handler.update(new Message(Status.GAME_FULL));
        }catch(GameException e){
            handler.update(new IntegerMessage(Status.INVALID_COLOR, gameInfo.getGameId()));
        }catch(NicknameException n){
            handler.update(new IntegerMessage(Status.INVALID_NICKNAME, gameInfo.getGameId()));
        }
    }

    /**
     * Adds a previously disconnected player back to the game. It sends to the player all the information needed to
     * restore his game status. Finally, it notifies the other player of the successful reconnection.
     *
     * @param nickname             the player to add back.
     * @param handler              the handler associated to the player.
     * @param sendTurnNotification whether the player who's getting reconnected should receive a notification
     *                             specifying whose turn is being played. this should be disabled every time a player
     *                             reconnects between two turns (for example, when a single player is left and their
     *                             turn has already been completed).
     */
    private void reconnectPlayer(String nickname, NetworkHandler handler, boolean sendTurnNotification){
        NetworkHandler userHandler = serverSubject.getNetworkHandler(nickname);
        if(userHandler == null || !userHandler.isDisconnected()){
            handler.update(new Message(Status.WRONG_NAME));
            return;
        }
        serverSubject.subscribe(nickname, handler);
        receivePing(handler);
        handler.setCurrentGame(this);
        synchronized (onlyOnePlayerLock){
            onlyOnePlayer = false;
        }
        serverSubject.notify(nickname, new JoinGameMessage(Status.JOIN_GAME, nickname,
                game.getPlayer(nickname).getColor(), game.getNumberOfPlayers(), gameInfo.getGameId()));
        int turnNumber = 1;
        for(Player player : game.getAllPlayers()){
            serverSubject.notify(nickname, new JoinGameMessage(Status.NEW_PLAYER_JOINED, player.getNickname(),
                    player.getColor(), turnNumber, gameInfo.getGameId()));
            turnNumber++;
        }
        serverSubject.notify(nickname, new DrawOptionsMessage(Status.DRAW_OPTIONS, game.getDrawableCards(), game.getNumberOfCardsLeft()));
        for(Player player : game.getAllPlayers()){
            serverSubject.notify(nickname, new StringMessage(Status.SILENT_TURN_NOTIFICATION, player.getNickname()));
            if(serverSubject.getNetworkHandler(player.getNickname()).isDisconnected()){
                serverSubject.notify(nickname, new StringMessage(Status.QUIET_PLAYER_DISCONNECTED, player.getNickname()));
            }
            if(player.getPlacedCards().isEmpty()){
                continue;
            }
            serverSubject.notify(nickname, new PlayerBoardMessage(player.getPlacedCards(), player.getScore()));
            if(player.getNickname().equals(nickname)){
                serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARDS, player.getHandCards()));
                int numberOfCommonObjectives = Parameters.getNumberOfCommonObjectives();
                int numberOfSecretObjectives = Parameters.getNumberOfSecretObjectives();
                if(player.getObjectives().size() == numberOfCommonObjectives + numberOfSecretObjectives){
                    serverSubject.notify(nickname, new ObjectivesMessage(Status.COMMON_OBJECTIVES,
                            game.getCommonObjectives()));
                    serverSubject.notify(nickname, new ObjectivesMessage(Status.SECRET_OBJECTIVES,
                            player.getObjectives().stream().filter(o -> !game.getCommonObjectives().contains(o)).toList()));
                }
            }else{
                serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_BACK, player.getBackOnlyCardHand()));
            }
        }
        serverSubject.notifyAll(new StringMessage(Status.RECONNECT, nickname));
        if(sendTurnNotification) {
            serverSubject.notify(nickname, new StringMessage(Status.TURN_NOTIFICATION, playerWithTurn));
        }
    }

    /**
     * Restarts the game as a player reconnects after just one client was left in the game.
     */
    public void wakeUpAfterReconnect(){
        synchronized (onlyOnePlayerLock){
            onlyOnePlayerLock.notifyAll();
        }
    }

    /**
     * Gets the game's name.
     *
     * @return the game's name.
     */
    public String getName(){
        return gameInfo.getGameName();
    }

    /**
     * Gets the game's status.
     *
     * @return the status of the game.
     *
     * @see GameStatus
     */
    public GameStatus getGameStatus(){
        synchronized (gameInfo) {
            return gameInfo.getGameStatus();
        }
    }

    /**
     * Adds a message to the message queue.
     *
     * @param message the message to add.
     * @param handler the player that sent the message.
     *
     * @see Message
     * @see NetworkHandler
     */
    public void addMessageToQueue(Message message, NetworkHandler handler){
        synchronized (messageQueue) {
            messageQueue.add(new LabeledMessage(handler, message));
        }
    }

    /**
     * Adds the player to the connected users list.
     *
     * @param networkHandler the player that received the ping.
     *
     * @see NetworkHandler
     */
    public void receivePing(NetworkHandler networkHandler){
        synchronized (connectedUsers){
            connectedUsers.add(networkHandler);
        }
    }

    /**
     * Polls a message from the message queue and returns it only if it is sent by the specified handler,
     * it ignores the message otherwise. If the handler is disconnected returns a PLAYER_DISCONNECTED message.
     * It doesn't return CHAT, RECONNECT, JOIN_GAME and REQUEST_COLORS messages as it handles them directly.
     *
     * @param handler the player from which the server expects a message.
     *
     * @return        the polled message.
     *
     * @see Status
     * @see NetworkHandler
     */
    private Message readFromQueue(NetworkHandler handler){
        LabeledMessage labeledMessage;
        while(handler != null && !handler.isDisconnected()){
            synchronized(messageQueue) {
                if (messageQueue.isEmpty()) {
                    Thread.onSpinWait();
                    continue;
                }
                labeledMessage = messageQueue.poll();
            }
            Message message = labeledMessage.message();
            //handle special messages
            switch (message.getStatus()){
                case CHAT -> {
                    if (message instanceof ChatMessage chatMessage) {
                        broadcastMessage(chatMessage, labeledMessage.networkHandler());
                        continue;
                    }
                }
                case RECONNECT -> {
                    if(message instanceof StringMessage stringMessage) {
                        reconnectPlayer(stringMessage.getString(), labeledMessage.networkHandler(), true);
                        continue;
                    }
                }
                case JOIN_GAME, REQUEST_COLORS -> labeledMessage.networkHandler().update(new Message(Status.GAME_FULL));
            }
            //ignore game messages sent by the other network handlers
            if(labeledMessage.networkHandler() != handler){
                continue;
            }
            //return the game message sent by the correct network handler
            return labeledMessage.message();
        }
        return new Message(Status.PLAYER_DISCONNECTED);
    }

    /**
     * Broadcasts a chat message.
     *
     * @param chatMessage the chat message.
     * @param handler     the handler who sent it.
     *
     * @see ChatMessage
     * @see NetworkHandler
     */
    private void broadcastMessage(ChatMessage chatMessage, NetworkHandler handler){
        String senderNickname = game.getAllPlayers().stream()
                .map(Player::getNickname)
                .filter(n -> serverSubject.getNetworkHandler(n) == handler)
                .findFirst().orElse("No one");
        int chatMsgLength = chatMessage.getMessage().length();
        List<String> recipients = chatMessage.getRecipients();
        Message messageToSendBack = new ChatMessage(
                chatMessage.getMessage().substring(0, Math.min(chatMsgLength, Parameters.getMaxChatMessageLength())),
                senderNickname,
                chatMessage.getRecipients());
        for (String nickname : recipients) {
            serverSubject.notify(nickname, messageToSendBack);
        }
        serverSubject.notify(senderNickname, messageToSendBack);
    }

    /**
     * Returns a list of the handlers that aren't in the connected users list.
     *
     * @return the disconnected handlers.
     *
     * @see NetworkHandler
     */
    private List<NetworkHandler> getDisconnectedHandlers(){
        return game.getLobbyNicknames().stream()
                .map(serverSubject::getNetworkHandler)
                .filter(n -> !connectedUsers.contains(n))
                .toList();
    }

    /**
     * Checks if any player has disconnected from the game and sends a ping request to all the clients.
     * If a player doesn't answer to the ping, sets his network handler to disconnected and notifies the others with
     * a PLAYER_DISCONNECTED message.
     * When the connected players are less than one, it sets the game over flag to true and stops the ping timer.
     * When there is only one player connected, it sets the only one player flag to true.
     *
     * @see NetworkHandler
     * @see Status
     */
    private void handleGameDisconnections(){
        List<NetworkHandler> disconnectedHandlers;
        //connected players part
        synchronized (connectedUsers) {
            if (connectedUsers.size() == 1) {
                synchronized (onlyOnePlayerLock) {
                    onlyOnePlayer = true;
                }
            }
            if(connectedUsers.isEmpty()){
                logger.info("Game " + gameInfo.getGameId() + ": No players connected, starting abort procedure.\n");
                gameOver.set(true);
                pingTimer.cancel();
                synchronized (onlyOnePlayerLock) {
                    onlyOnePlayerLock.notifyAll();
                }
            }
            disconnectedHandlers = getDisconnectedHandlers();
            connectedUsers.clear();
            //notifying part
            for(NetworkHandler networkHandler : disconnectedHandlers){
                synchronized (gameInfo) {
                    gameInfo.setGameStatus(GameStatus.PLAYER_DISCONNECTED);
                }
                if(networkHandler == null){
                    continue;
                }
                if(!networkHandler.isDisconnected()){
                    serverSubject.notifyAll(new StringMessage(Status.PLAYER_DISCONNECTED,
                            game.getAllPlayers().stream()
                                    .map(Player::getNickname)
                                    .filter(n -> serverSubject.getNetworkHandler(n) == networkHandler)
                                    .findFirst().orElse("No players")));
                }
                networkHandler.setDisconnected();
            }
            if(disconnectedHandlers.isEmpty()){
                synchronized (gameInfo){
                    gameInfo.setGameStatus(GameStatus.STARTED);
                }
            }
        }
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
    }

    /**
     * Resets all the information about a player that has disconnected from the lobby; finally requests a ping to
     * each client.
     */
    private void handleLobbyDisconnections(){
        List<NetworkHandler> disconnectedHandlers;
        synchronized (connectedUsers) {
            disconnectedHandlers = getDisconnectedHandlers();
            connectedUsers.clear();
            for(NetworkHandler networkHandler : disconnectedHandlers){
                removePlayerFromLobby(networkHandler);
            }
        }
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
    }

    /**
     * Removes a player from the lobby.
     *
     * @param networkHandler the player's network handler.
     */
    private void removePlayerFromLobby(NetworkHandler networkHandler){
        String playerNickname = game.getLobbyNicknames().stream()
                .filter(n -> serverSubject.getNetworkHandler(n) == networkHandler)
                .findFirst().orElse("No players");
        serverSubject.getNetworkHandler(playerNickname).setCurrentGame(null);
        game.deletePlayerData(playerNickname);
    }

    /**
     * Removes all the players from the game.
     */
    private void removeHandlers(){
        pingTimer.cancel();
        for (Player player : game.getAllPlayers()) {
            NetworkHandler playerHandler = serverSubject.getNetworkHandler(player.getNickname());
            if(playerHandler != null) {
                playerHandler.setCurrentGame(null);
                serverSubject.unsubscribe(player.getNickname());
            }
        }
    }

    /**
     * Checks if there's only one player left in the game.
     * If that's true and the game isn't already over, starts a timer at the end of which the game ends and the
     * remaining player is declared the winner.
     * The game thread will be awakened before the end of the timer if the only player remaining leaves or a disconnected
     * player reconnects.
     */
    private void checkForOnlyOnePlayer() {
        synchronized (onlyOnePlayerLock) {
            if (onlyOnePlayer && !gameOver.get()) {
                List<String> playerLeft = game.getAllPlayers().stream().map(Player::getNickname)
                        .filter(n -> !serverSubject.getNetworkHandler(n).isDisconnected())
                        .toList();
                serverSubject.notifyAll(new Message(Status.GAME_TIMEOUT_STARTED));
                logger.info(String.format("Game %d: only one player left, the game will end in %d seconds.\n",
                        gameInfo.getGameId(), Parameters.getForfeitTime()));
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (onlyOnePlayerLock){
                            gameOver.set(true);
                            pingTimer.cancel();
                            serverSubject.notifyAll(new WinnersMessage(playerLeft));
                            removeHandlers();
                            onlyOnePlayerLock.notifyAll();
                        }
                    }
                }, Parameters.getForfeitTime() * 1000L);
                try {
                    onlyOnePlayerLock.wait();
                    timer.cancel();
                    if(!gameOver.get()){
                        synchronized (messageQueue){
                            LabeledMessage message;
                            while((message = messageQueue.poll()) != null){
                                if(message.message().getStatus() == Status.RECONNECT &&
                                        message.message() instanceof StringMessage reconnectMessage) {
                                    reconnectPlayer(reconnectMessage.getString(), message.networkHandler(), false);
                                    break;
                                }
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    logger.severe(String.format("Game %d: %s", gameInfo.getGameId(), e.getMessage() + "\n"));
                }
            }
        }
    }

    /**
     * Sets the turn to the specified player and notifies all players.
     *
     * @param nickname the active player.
     */
    private void updateTurn(String nickname){
        playerWithTurn = nickname;
        serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, nickname));
    }

    /**
     * Handles the setup of the game.
     * Creates the actual players in the lobby, starts the game ping procedure
     * and makes all players place their starter card as well as choosing their personal objective.
     * Also checks if there's only one player left at the beginning of each turn as well as the game over flag at then
     * end of each turn.
     */
    private void initializeGame() {
        game.createPlayers();
        pingTimer.cancel();
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                handleGameDisconnections();
            }
        };
        int periodSeconds = Parameters.getServerPingPeriodSeconds();
        pingTimer = new Timer();
        pingTimer.schedule(pingTask, periodSeconds * 1000L, periodSeconds * 1000L);
        Map<CardType, List<BasicCard>> cards = game.getDrawableCards();
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, cards, game.getNumberOfCardsLeft()));
        for (Player player : game.getAllPlayers()) {
            checkForOnlyOnePlayer();
            updateTurn(player.getNickname());
            placeStarterCard(player);
            if(gameOver.get()){
                return;
            }
        }
        for (Player player : game.getAllPlayers()) {
            checkForOnlyOnePlayer();
            updateTurn(player.getNickname());
            choosePersonalObjective(player);
            if(gameOver.get()){
                return;
            }
        }
    }

    /**
     * Sends to the specified player his hand and requests him to choose which side of the starter card he wants to place.
     * Receives his choice about the starter card side to place and places it; if the player isn't connected
     * automatically places the card.
     *
     * @param player the player that is placing the starter card.
     */
    private void placeStarterCard(Player player){
        CardSides starterCard = player.getHandCards().getFirst();
        Status currentStatus = Status.STARTER_CARD;
        BasicCard starterSide = null;
        while (!starterCard.frontSide().equals(starterSide) && !starterCard.backSide().equals(starterSide)){
            serverSubject.notify(player.getNickname(), new CardHandMessage(currentStatus, player.getHandCards()));
            Message message = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (message instanceof CardPlacementMessage cardPlacementMessage){
                starterSide = cardPlacementMessage.getCard();
            } else if (message.getStatus() == Status.PLAYER_DISCONNECTED){
                starterSide = starterCard.frontSide();
                serverSubject.notifyAll(new Message(Status.TURN_SKIPPED));
            }
            currentStatus = Status.INVALID_STARTER_CARD;
        }
        player.placeStarterCard(starterSide);
    }

    /**
     * Sends to the specified player his common objectives and the secret objectives to choose from.
     * Requests him to choose a secret objective.
     * Receives his choice about the personal objective and updates the game's model; if the player isn't connected
     * automatically chooses the first objective.
     *
     * @param player the player that is choosing the personal objective.
     */
    @SuppressWarnings({"SlowListContainsAll"})
    private void choosePersonalObjective(Player player){
        List<Objective> drawnObjectives = game.drawObjectiveCards();
        Status currentStatus = Status.REQUEST_SECRET_OBJECTIVES;
        List<Objective> secretObjectives = new ArrayList<>();
        List<Objective> chosenObjectives = new ArrayList<>(drawnObjectives);
        serverSubject.notify(player.getNickname(), new ObjectivesMessage(Status.COMMON_OBJECTIVES, game.getCommonObjectives()));
        while(secretObjectives.isEmpty() || !drawnObjectives.containsAll(secretObjectives)){
            serverSubject.notify(player.getNickname(), new ObjectivesMessage(currentStatus, drawnObjectives));
            Message message = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (message instanceof ObjectivesMessage objectiveMessage){
                secretObjectives = objectiveMessage.getObjectives();
            } else if (message.getStatus() == Status.PLAYER_DISCONNECTED){
                secretObjectives.addAll(drawnObjectives.subList(0, Parameters.getNumberOfSecretObjectives()));
                serverSubject.notifyAll(new Message(Status.TURN_SKIPPED));
            }
            chosenObjectives = new ArrayList<>(drawnObjectives);
            chosenObjectives = chosenObjectives.stream().filter(secretObjectives::contains).toList();
            if(chosenObjectives.size() != Parameters.getNumberOfSecretObjectives()){
                continue;
            }
            currentStatus = Status.INVALID_SECRET_OBJECTIVES;
        }
        player.addPersonalObjectives(chosenObjectives);
    }

    /**
     * Handles the game's progression.
     * Cycles between the players and makes them play their turn, including the last one; computes the final scores
     * and declares the winner.
     * Also checks if the game is stuck (all players can't make a valid move), if there is only one player left at the
     * beginning of each turn and if the game over flag is true.
     * Finally, reveals the winners.
     */
    private void startGame() {
        while (!game.isLastTurn() && !game.isGameStuck()) {
            for (Player player : game.getAllPlayers()) {
                checkForOnlyOnePlayer();
                updateTurn(player.getNickname());
                if(player.isPlayerStuck()){
                    serverSubject.notifyAll(new Message(Status.NO_MOVES));
                    continue;
                }
                if(placeCard(player)) {
                    drawCard(player);
                }
                if(gameOver.get()){
                    return;
                }
            }
        }
        //last turn of the game
        serverSubject.notifyAll(new Message(Status.LAST_TURN));
        for (Player player : game.getAllPlayers()) {
            if(game.isGameStuck()){
                break;
            }
            checkForOnlyOnePlayer();
            updateTurn(player.getNickname());
            if(player.isPlayerStuck()){
                serverSubject.notifyAll(new Message(Status.NO_MOVES));
                continue;
            }
            placeCard(player);
            if(gameOver.get()){
                return;
            }
        }
        //calculate the final score
        Map<String, Integer> completedObjectives = new HashMap<>();
        for (Player player : game.getAllPlayers()){
            completedObjectives.put(
                    player.getNickname(),
                    player.awardObjectivePoints().stream().filter(n -> n != 0).toList().size());
        }
        List<String> winners = game.getWinningPlayers();
        List<String> bestObjectiveScorer = winners.stream()
                .filter(p -> completedObjectives.values().stream()
                        .max(Integer::compareTo)
                        .orElseThrow()
                        .equals(completedObjectives.get(p)))
                .toList();
        serverSubject.notifyAll(new WinnersMessage(winners.size() > 1 ? bestObjectiveScorer : winners));
    }

    /**
     * Sends to the specified player his placeable cards and his valid corners.
     * Requests him to pick one card to place and one corner.
     * Receives his choice about the card to place and where to place it: after checking if the placement is valid,
     * places the card; if the player isn't connected, skips his turn.
     *
     * @param player the player that is placing a card.
     * @return       false if the player disconnected.
     */
    private boolean placeCard(Player player) {
        List<Corner> validPlacements = player.getAllValidCorners();
        serverSubject.notify(player.getNickname(),
                new ValidPlacementsMessage(Status.PLACE_CARD, player.getAllValidCards(), validPlacements));
        BasicCard cardToPlace = null;
        Corner chosenCorner = null;
        boolean moveValid = false;
        while (!moveValid) {
            Message message = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (message instanceof CardPlacementMessage cardPlacementMessage) {
                BasicCard cardToLookFor = cardPlacementMessage.getCard();
                cardToPlace = player.getAllValidCards().stream()
                        .filter(c -> c.equals(cardToLookFor))
                        .findFirst()
                        .orElse(null);
                chosenCorner = cardPlacementMessage.getCorner();
            } else if (message.getStatus() == Status.PLAYER_DISCONNECTED){
                serverSubject.notifyAll(new Message(Status.TURN_SKIPPED));
                return false;
            }
            moveValid = isMoveValid(player, cardToPlace, chosenCorner);
            if (!moveValid){
                serverSubject.notify(player.getNickname(),
                        new ValidPlacementsMessage(Status.INVALID_PLACE_CARD, player.getAllValidCards(), validPlacements));
            }
        }
        player.placeCard(cardToPlace, chosenCorner);
        return true;
    }

    /**
     * Checks if the player's choice about a card placement is valid.
     *
     * @param player the player that is placing a card.
     * @param card   the card chosen.
     * @param corner the corner chosen.
     *
     * @return       true if the move is valid.
     */
    private boolean isMoveValid(Player player, BasicCard card, Corner corner){
        return (card != null &&
                player.checkRequirements(card) &&
                player.isCardInHand(card) &&
                corner != null &&
                player.checkIfPlaceable(corner) &&
                player.isCornerPartOfBoard(corner));
    }

    /**
     * Sends to the specified player the current drawable cards.
     * Requests him to pick one card from the drawable ones.
     * Receives his choice about where to draw the card and adds it to the player's hand; if the player isn't
     * connected draws the first available card.
     *
     * @param player the player that is drawing.
     */
    private void drawCard(Player player) {
        if(game.getDrawableCards().values().stream().allMatch(e -> e.getFirst() == null && e.size() == 1)){
            return;
        }
        boolean drawSuccess = false;
        Status currentStatus = Status.DRAW;
        while (!drawSuccess){
            CardType typeChosen = null;
            int indexChosen = -1;
            do{
                serverSubject.notify(player.getNickname(), new DrawOptionsMessage(currentStatus, game.getDrawableCards(), game.getNumberOfCardsLeft()));
                currentStatus = Status.INVALID_DRAW;
                Message message = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
                if (message instanceof DrawChoiceMessage drawChoiceMessage){
                    indexChosen = drawChoiceMessage.getIndex();
                    typeChosen = drawChoiceMessage.getCardType();
                    continue;
                }
                if (message.getStatus() == Status.PLAYER_DISCONNECTED){
                    for(Map.Entry<CardType, List<BasicCard>> entry : game.getDrawableCards().entrySet()){
                        boolean found = false;
                        for(int i = 0; i < entry.getValue().size(); i++){
                            BasicCard card = entry.getValue().get(i);
                            if(card != null){
                                typeChosen = entry.getKey();
                                indexChosen = i;
                                found = true;
                                break;
                            }
                        }
                        if(found){
                            break;
                        }
                    }
                }
            }while(typeChosen == null || indexChosen < 0 || indexChosen > Parameters.getNumberOfVisibleCards());
            try{
                drawSuccess = true;
                game.drawCard(player, typeChosen, indexChosen);
            }catch (GameException e) {
                drawSuccess = false;
            }
        }
    }

    /**
     * Handles the game when the players are in lobby waiting for it to be full.
     * Accepts a new player connecting to the game until the game is full.
     * Also handles disconnection from the lobby by removing the player from the lobby and notifying all other players.
     * If the game over flag is true, returns.
     */
    private void waitForPlayers(){
        LabeledMessage labeledMessage;
        while(!game.isGameFull() && !gameOver.get()){
            synchronized (messageQueue) {
                if (messageQueue.isEmpty()) {
                    Thread.onSpinWait();
                    continue;
                }
                labeledMessage = messageQueue.poll();
            }
            Message message = labeledMessage.message();
            switch (message.getStatus()){
                case Status.JOIN_GAME -> {
                    if (message instanceof JoinGameMessage joinGameMessage) {
                        acceptPlayer(
                                joinGameMessage.getNickname(),
                                joinGameMessage.getColor(),
                                labeledMessage.networkHandler());
                    }
                }
                case Status.REQUEST_COLORS ->
                    labeledMessage.networkHandler().update(
                            new GameColorsMessage(Status.REQUEST_COLORS, game.getAvailableColors(), gameInfo.getGameId()));
                case Status.PLAYER_DISCONNECTED -> removePlayerFromLobby(labeledMessage.networkHandler());
            }
        }
    }

    /**
     * Handles the lobby of the game.
     * Periodically sends pings to all the clients and, if one or more disconnected, handles the disconnections; if all
     * clients disconnected, ends the game.
     */
    private void startLobby(){
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                handleLobbyDisconnections();
            }
        };
        pingTimer = new Timer();
        pingTimer.schedule(pingTask,
                Parameters.getServerPingPeriodSeconds() * 1000L,
                Parameters.getServerPingPeriodSeconds() * 1000L);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                GameStatus status = GameStatus.LOBBY;
                while(status != GameStatus.STARTED && !gameOver.get()){
                    synchronized (gameInfo) {
                        status = gameInfo.getGameStatus();
                    }
                    if(game.isLobbyEmpty()) {
                        removeHandlers();
                        gameOver.set(true);
                        pingTimer.cancel();
                        endGameProcedure.accept(GameController.this);
                    }
                }
                }
            }, Parameters.getLobbyTimeout() * 1000L);
    }

    /**
     * Waits until there are enough players, then starts the match.
     * When the game ends, uses the consumer to delete the game's controller.
     */
    @Override
    public void run() {
        startLobby();
        waitForPlayers();
        if(gameOver.get()){
            return;
        }
        synchronized (gameInfo) {
            gameInfo.setGameStatus(GameStatus.STARTED);
        }
        logger.info("Game " + gameInfo.getGameId() + " has started.\n");
        initializeGame();
        startGame();
        removeHandlers();
        endGameProcedure.accept(this);
    }

    /**
     * Equals method.
     *
     * @param object object to check.
     *
     * @return       true if both controllers have equals game info.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof GameController other){
            return this.gameInfo.equals(other.gameInfo);
        }
        return false;
    }
}