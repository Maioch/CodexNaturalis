package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.*;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.PlayerMessage;
import it.polimi.ingsw.network.server.ServerSubject;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameModel;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * GameController is the class that represents the MVC pattern controller.
 * Each game has one: it allows to change the status of the match model after the occurrence of a player action,
 * and it can also check for special configurations in it.
 */
public class GameController implements Runnable{
    private final GameModel game;
    private final String name;
    private final ServerSubject serverSubject;
    private final Queue<LabeledMessage> messageQueue;
    private final List<NetworkHandler> connectedUsers;
    private final Consumer<GameController> endGameProcedure;
    private final Object gameIsFullLock;
    private final Object gameStatusLock;
    private final Object onlyOnePlayerLock;
    private final AtomicBoolean gameOver;
    private final Timer pingTimer;
    private String playerWithTurn;
    private GameStatus gameStatus;
    private boolean onlyOnePlayer;

    /**
     * Class constructor.
     *
     * @param numberOfPlayers         the maximum number of players that can join the game.
     * @param serverSubject           the object used to notify about a change in the game's model.
     * @param name                    the name of the game.
     * @param endGameProcedure        the consumer used to delete the game controller when the game ends.
     *
     * @throws IllegalNumberOfPlayers if the player entered an invalid maximum number of players when creating the game.
     */
    public GameController(int numberOfPlayers,
                          ServerSubject serverSubject,
                          String name,
                          Consumer<GameController> endGameProcedure) throws IllegalNumberOfPlayers {
        this.game = new GameModel(numberOfPlayers, serverSubject);
        this.name = name;
        this.serverSubject = serverSubject;
        this.messageQueue = new LinkedList<>();
        this.connectedUsers = new ArrayList<>();
        this.endGameProcedure = endGameProcedure;
        this.gameIsFullLock = new Object();
        this.gameStatusLock = new Object();
        this.onlyOnePlayerLock = new Object();
        this.onlyOnePlayer = false;
        this.gameOver = new AtomicBoolean(false);
        this.gameStatus = GameStatus.LOBBY;
        this.pingTimer = new Timer();
        this.playerWithTurn = "";
    }

    /**
     * Returns the list of colors from which a new player entering the game may choose.
     *
     * @return all the game's available colors.
     */
    public List<Content> requestAvailableColors(){
        return new ArrayList<>(game.getAvailableColors());
    }

    /**
     * Adds a player to the game and then, if the game is full, sends a notification to start the game.
     * If the nickname isn't already taken, adds the new player to the list of server subjects.
     *
     * @param nickname                the nickname of the player.
     * @param color                   the color chosen by the player.
     * @param handler                 the TCP/RMI handler associated to the new player.
     *
     * @throws GameFullException      if the game is already full.
     * @throws NicknameTakenException if the chosen nickname is already taken.
     * @throws GameException          if the chosen color is already taken.
     *
     * @see NetworkHandler
     */
    public void acceptPlayer(String nickname, Content color, NetworkHandler handler) throws GameFullException, NicknameTakenException, GameException{
        if(game.checkNickname(nickname)){
            serverSubject.subscribe(nickname, handler);
        }
        try {
            game.addPlayer(nickname, color);
        }catch(GameFullException | GameException e) {
            serverSubject.unsubscribe(nickname);
            throw e;
        }
        if(isGameFull()){
            synchronized(gameIsFullLock) {
                gameIsFullLock.notifyAll();
            }
        }
    }

    /**
     * Adds a previously disconnected player back to the game.
     *
     * @param nickname the player to add back.
     * @param handler  the handler associated to the player.
     */
    private void reconnectPlayer(String nickname, NetworkHandler handler){
        NetworkHandler userHandler = serverSubject.getNetworkHandler(nickname);
        if(userHandler == null || !userHandler.isDisconnected()){
            handler.update(new Message(Status.ERROR));
            return;
        }
        serverSubject.subscribe(nickname, handler);
        serverSubject.notify(nickname, new PlayerMessage(Status.JOIN_GAME, nickname, game.getPlayer(nickname).getColor()));
        serverSubject.notify(nickname, new DrawOptionsMessage(Status.DRAW_OPTIONS, game.getDrawableCards()));
        for(Player player : game.getAllPlayers()){
            serverSubject.notify(nickname, new PlayerMessage(Status.NEW_PLAYER_JOINED, player.getNickname(), player.getColor()));
        }
        for(Player player : game.getAllPlayers()){
            serverSubject.notify(nickname, new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            serverSubject.notify(nickname, new PlayerBoardMessage(player.getPlacedCards(), player.getScore()));
            if(player.getNickname().equals(nickname)){
                serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARDS, player.getHandCards()));
                serverSubject.notify(nickname, new ObjectivesMessage(Status.ALL_OBJECTIVES,
                        player.getObjectives().stream().filter(o -> !game.getCommonObjectives().contains(o)).toList(),
                        game.getCommonObjectives()));
            }else{
                serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_BACK, player.getBackOnlyCardHand()));
            }
        }
        serverSubject.notify(nickname, new StringMessage(Status.TURN_NOTIFICATION, playerWithTurn));
    }

    /**
     * Checks if the game is full
     *
     * @return true if the game is full.
     */
    public boolean isGameFull() {
        return game.isGameFull();
    }

    /**
     * Gets the game's name.
     *
     * @return the game's name.
     */
    public String getName(){
        return name;
    }

    /**
     * Gets the game's status.
     *
     * @return the status of the game.
     */
    public GameStatus getGameStatus(){
        synchronized (gameStatusLock) {
            return gameStatus;
        }
    }

    /**
     * Adds a message to the message queue.
     *
     * @param message the message to add.
     * @param handler the player that sent the message.
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
     */
    public void receivePing(NetworkHandler networkHandler){
        synchronized (connectedUsers){
            connectedUsers.add(networkHandler);
        }
    }

    /**
     * Polls a message from the message queue.
     * If it's a chat message, sends it to the corresponding recipients, and then polls another message.
     * This method implements a timer.
     *
     * @param  handler the player from which the server expects a message.
     *
     * @return the polled message.
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
                        reconnectPlayer(stringMessage.getString(), labeledMessage.networkHandler());
                        continue;
                    }
                }
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
     * @param handler the handler who sent it.
     */
    private void broadcastMessage(ChatMessage chatMessage, NetworkHandler handler){
        String senderNickname = game.getAllPlayers().stream()
                .map(Player::getNickname)
                .filter(n -> serverSubject.getNetworkHandler(n) == handler)
                .findFirst().orElse("No one");
        int chatMsgLength = chatMessage.getMessage().length();
        List<String> recipients = chatMessage.getRecipients();
        Message messageToSendBack = new ChatMessage(
                chatMessage.getMessage().substring(0, Math.min(chatMsgLength, GameParameters.getMaxChatMessageLength())),
                senderNickname,
                chatMessage.getRecipients());
        for (String nickname : recipients) {
            serverSubject.notify(nickname, messageToSendBack);
        }
        serverSubject.notify(senderNickname, messageToSendBack);
    }

    /**
     * Waits that all clients are ready to play the game, using the CLIENT_READY message.
     *
     * @param handlers all the players connected to the game.
     */
    private void awaitForReady(List<NetworkHandler> handlers){
        LabeledMessage labeledMessage;
        List<NetworkHandler> handlerList = new ArrayList<>(handlers);
        while(!handlerList.isEmpty()){
            synchronized(messageQueue) {
                if (messageQueue.isEmpty()) {
                    Thread.onSpinWait();
                    continue;
                }
                labeledMessage = messageQueue.poll();
            }
            Message message = labeledMessage.message();
            NetworkHandler sender = labeledMessage.networkHandler();
            handlerList.removeIf(h -> message.getStatus() == Status.CLIENT_READY && h == sender);
        }
    }

    /**
     * Checks if any player has disconnected from the game and sends a ping request to all the clients.
     * If a player doesn't answer to the ping, labels him as "disconnected", then notifies all the others with
     * a PLAYER_DISCONNECTED message.
     * When the connected players are less than or equal to one, the game will start an ending procedure.
     */
    private void readAndRequestPings(){
        List<NetworkHandler> disconnectedHandlers;
        //connected players part
        synchronized (connectedUsers) {
            if (connectedUsers.size() == 1) {
                synchronized (onlyOnePlayerLock) {
                    onlyOnePlayer = true;
                }
            }
            if(connectedUsers.isEmpty()){
                System.out.println("No players connected, starting abort procedure");
                gameOver.set(true);
                pingTimer.cancel();
                synchronized (onlyOnePlayerLock) {
                    onlyOnePlayerLock.notifyAll();
                }
            }
            disconnectedHandlers = game.getAllPlayers().stream()
                    .map(p -> serverSubject.getNetworkHandler(p.getNickname()))
                    .filter(n -> !connectedUsers.contains(n))
                    .toList();
            connectedUsers.clear();
        }
        //notifying part
        for(NetworkHandler networkHandler : disconnectedHandlers){
            if(!networkHandler.isDisconnected()){
                serverSubject.notifyAll(new StringMessage(Status.PLAYER_DISCONNECTED,
                        game.getAllPlayers().stream()
                                .map(Player::getNickname)
                                .filter(n -> serverSubject.getNetworkHandler(n) == networkHandler)
                                .findFirst().orElse("No players")));
            }
            networkHandler.setDisconnected();
        }
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
    }

    /**
     * Removes all the players from the game.
     */
    private void removeHandlers(){
        for (Player player : game.getAllPlayers()) {
            NetworkHandler playerHandler = serverSubject.getNetworkHandler(player.getNickname());
            if(playerHandler != null) {
                playerHandler.setCurrentGame(null);
                serverSubject.unsubscribe(player.getNickname());
            }
        }
    }

    /**
     * Starts a timer at the end of which the lobby is dismantled.
     */
    private void startLobbyTimer(){
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (gameStatusLock){
                    if(gameStatus == GameStatus.LOBBY){
                        serverSubject.notifyAll(new Message(Status.GAME_CANCELED));
                        removeHandlers();
                        gameOver.set(true);
                        endGameProcedure.accept(GameController.this);
                        synchronized (gameIsFullLock){
                            gameIsFullLock.notifyAll();
                        }
                    }
                }
            }
        }, GameParameters.getLobbyTimeout() * 1000L);
    }

    /**
     * Checks if there's only one player left in the game.
     * If that's true and the game isn't already over, starts a timer at the end of which the game ends and the
     * remaining player is declared the winner.
     */
    private void checkForOnlyOnePlayer() {
        synchronized (onlyOnePlayerLock) {
            if (onlyOnePlayer && !gameOver.get()) {
                List<String> playerLeft = game.getAllPlayers().stream().map(Player::getNickname)
                        .filter(n -> !serverSubject.getNetworkHandler(n).isDisconnected())
                        .toList();
                serverSubject.notifyAll(new Message(Status.GAME_TIMEOUT_STARTED));
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        gameOver.set(true);
                        pingTimer.cancel();
                        serverSubject.notifyAll(new WinnersMessage(playerLeft));
                        removeHandlers();
                        synchronized(onlyOnePlayerLock) {
                            onlyOnePlayerLock.notifyAll();
                        }
                    }
                }, GameParameters.getForfeitTime() * 1000L);
                try {
                    System.out.println("Only one player left, the game will end in " + GameParameters.getForfeitTime() + " seconds");
                    onlyOnePlayerLock.wait();
                    timer.cancel();
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }
    
    private void updateTurn(String nickname){
        playerWithTurn = nickname;
        serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, nickname));
    }

    /**
     * Handles the setup of the game.
     * First checks if all the players are actually connected to the game, then makes them place their starter card and
     * choose their personal objective.
     */
    private void initializeGame() {
        serverSubject.notifyAll(new Message(Status.REQUEST_PING));
        TimerTask pingTask = new TimerTask() {
            @Override
            public void run() {
                readAndRequestPings();
            }
        };
        int periodSeconds = GameParameters.getPingPeriodSeconds();
        pingTimer.schedule(pingTask, periodSeconds * 1000L, periodSeconds * 1000L);
        Map<CardType, List<BasicCard>> cards = game.getDrawableCards();
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, cards));
        for (Player player : game.getAllPlayers()) {
            updateTurn(player.getNickname());
            placeStarterCard(player);
            checkForOnlyOnePlayer();
            if(gameOver.get()){
                return;
            }
        }
        for (Player player : game.getAllPlayers()) {
            updateTurn(player.getNickname());
            choosePersonalObjective(player);
            checkForOnlyOnePlayer();
            if(gameOver.get()){
                return;
            }
        }
    }

    /**
     * Handles the starter card placement.
     * Gets the player's choice about the starter card side to place and places it; if the player isn't connected
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
     * Handles the personal objective choice.
     * Gets the player's choice about the personal objective and updates the game's model; if the player isn't connected
     * automatically chooses the first objective.
     *
     * @param player the player that is choosing the personal objective.
     */
    @SuppressWarnings({"SlowListContainsAll"})
    private void choosePersonalObjective(Player player){
        List<Objective> drawnObjectives = game.drawObjectiveCards();
        Status currentStatus = Status.SECRET_OBJECTIVES;
        List<Objective> secretObjectives = new ArrayList<>();
        while(secretObjectives.isEmpty() || !drawnObjectives.containsAll(secretObjectives)){
            serverSubject.notify(player.getNickname(), new ObjectivesMessage(currentStatus, drawnObjectives, new ArrayList<>()));
            Message message = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (message instanceof ObjectivesMessage objectiveMessage){
                secretObjectives = objectiveMessage.getPersonalObjectives();
            } else if (message.getStatus() == Status.PLAYER_DISCONNECTED){
                secretObjectives.addAll(drawnObjectives.subList(0, GameParameters.getNumberOfSecretObjectives()));
                serverSubject.notifyAll(new Message(Status.TURN_SKIPPED));
            }
            currentStatus = Status.INVALID_SECRET_OBJECTIVES;
        }
        player.addPersonalObjectives(secretObjectives);
    }

    /**
     * Handles the game's progression.
     * Cycles between the players and makes them play their turn, including the last one; computes the final scores
     * and declares the winner.
     * Also checks if a player is ever stuck (can't make any move).
     */
    private void startGame() {
        while (!game.isLastTurn() && !game.isGameStuck()) {
            for (Player player : game.getAllPlayers()) {
                updateTurn(player.getNickname());
                if(player.isPlayerStuck()){
                    serverSubject.notifyAll(new Message(Status.NO_MOVES));
                    continue;
                }
                if(placeCard(player)) {
                    drawCard(player);
                }
                checkForOnlyOnePlayer();
                if(gameOver.get()){
                    return;
                }
            }
        }
        //last turn of the game
        for (Player player : game.getAllPlayers()) {
            if(game.isGameStuck()){
                break;
            }
            serverSubject.notifyAll(new Message(Status.LAST_TURN));
            updateTurn(player.getNickname());
            if(player.isPlayerStuck()){
                serverSubject.notifyAll(new Message(Status.NO_MOVES));
                continue;
            }
            placeCard(player);
            checkForOnlyOnePlayer();
            if(gameOver.get()){
                return;
            }
        }
        //calculate the final score
        for (Player player : game.getAllPlayers()){
            updateTurn(player.getNickname());
            player.awardObjectivePoints();
        }
        List<String> winners = game.getWinningPlayers();
        serverSubject.notifyAll(new WinnersMessage(winners));
    }

    /**
     * Handles a generic card placement.
     * Gets the player's choice about the card to place and where to place it: after checking if the placement is valid,
     * places the card; if the player isn't connected, skips his turn.
     *
     * @param player the player that is placing a card.
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
     * Handles the drawing of a card.
     * Gets the player's choice about where to draw the card and adds it to the player's hand; if the player isn't
     * connected draws the first available card.
     *
     * @param player the player that is drawing.
     */
    private void drawCard(Player player) {
        boolean drawSuccess = false;
        Status currentStatus = Status.DRAW;
        if(game.getDrawableCards().values().stream().allMatch(e -> e.getFirst() == null && e.size() == 1)){
            return;
        }
        while (!drawSuccess){
            CardType typeChosen = null;
            int indexChosen = -1;
            do{
                serverSubject.notify(player.getNickname(), new DrawOptionsMessage(currentStatus, game.getDrawableCards()));
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
            }while(typeChosen == null || indexChosen < 0 || indexChosen > GameParameters.getNumberOfVisibleCards());
            try{
                drawSuccess = true;
                game.drawCard(player, typeChosen, indexChosen);
            }catch (GameException e) {
                drawSuccess = false;
            }
        }
    }

    /**
     * Calls all the above methods to correctly run a game.
     * First starts a timer to avoid a too long game setup procedure, waits that all the players connected are ready
     * and starts then runs the match.
     * When the game ends, uses the consumer to delete the game's controller.
     */
    @Override
    public void run() {
        startLobbyTimer();
        try {
            synchronized(gameIsFullLock) {
                gameIsFullLock.wait();
            }
            if(gameOver.get()){
                return;
            }
            awaitForReady(game.getAllPlayers().stream().map(p -> serverSubject.getNetworkHandler(p.getNickname())).toList());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        synchronized (gameStatusLock) {
            gameStatus = GameStatus.STARTED;
        }
        initializeGame();
        startGame();
        removeHandlers();
        endGameProcedure.accept(this);
    }
}