package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.*;
import it.polimi.ingsw.network.messages.generic.StringMessage;
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
import java.util.function.Consumer;


/**
 * Class that represents the controller for each game, according to the MVC model.
 *
 * @author Andrea Fidanza, Guglielmo Gatti, Marco Maiocchi, Francesco Saverio Nisoli
 */
public class GameController implements Runnable{
    private final GameModel game;
    private final String name;
    private final ServerSubject serverSubject;
    private final Queue<LabeledMessage> messageQueue;
    private final Consumer<GameController> endGameProcedure;

    /**
     * Constructor for the class.
     * @param numberOfPlayers the maximum number of players that can join the game.
     * @param serverSubject the object used to notify the serverListeners.
     * @param name the name of the game.
     * @param endGameProcedure the consumer for the class; it's used to delete the game controller when the match ends.
     * @throws IllegalNumberOfPlayers exception thrown if the player entered an invalid players number parameter.
     */
    public GameController(int numberOfPlayers,
                          ServerSubject serverSubject,
                          String name,
                          Consumer<GameController> endGameProcedure) throws IllegalNumberOfPlayers {
        this.game = new GameModel(numberOfPlayers, serverSubject);
        this.name = name;
        this.serverSubject = serverSubject;
        this.messageQueue = new LinkedList<>();
        this.endGameProcedure = endGameProcedure;
    }

    /**
     * @return the list of the game's available colors.
     */
    public List<Content> requestColors(){
        return new ArrayList<>(game.getAvailableColors());
    }

    /**
     * Method used to add a player to a game, and it subscribes him to the notification system.
     * @param nickname the nickname of the player that is joining the game.
     * @param color the color chosen by the player.
     * @param handler the TCP/RMI handler for the new player, that allows him to interact with the controller.
     * @throws GameFullException exception thrown if the game the player is trying to join is full.
     * @throws NicknameTakenException exception thrown if the nickname chosen by the player is already present in the game.
     * @throws GameException exception thrown if the color chosen by the player is already taken.
     */
    public void acceptPlayer(String nickname, Content color, NetworkHandler handler) throws GameFullException, NicknameTakenException, GameException{
        serverSubject.subscribe(nickname, handler);
        try {
            game.addPlayer(nickname, color);
        }catch(GameFullException | NicknameTakenException | GameException e) {
            serverSubject.unsubscribe(nickname);
            throw e;
        }
    }

    /**
     * Method that handles the first phase of the game: it makes the player place his starter card (after the card's side
     * is chosen) and informs the player about his objectives.
     */
    private void initializeGame() {
        Map<CardType, List<BasicCard>> cards = game.getDrawableCards();
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, cards));
        for (Player player : game.getAllPlayers()) {
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            CardSides starterCard = player.getHandCards().getFirst();
            Status currentStatus = Status.STARTER_CARD;
            BasicCard starterSide = null;
            while (!starterCard.frontSide().equals(starterSide) && !starterCard.backSide().equals(starterSide)){
                serverSubject.notify(player.getNickname(), new CardHandMessage(currentStatus, player.getHandCards()));
                LabeledMessage labeledMessage = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
                if (labeledMessage.message() instanceof CardPlacementMessage cardPlacementMessage){
                    starterSide = cardPlacementMessage.getCard();
                }
                currentStatus = Status.INVALID_STARTER_CARD;
            }
            player.placeStarterCard(starterSide);
            List<Objective> secretObjectives = player.getObjectives().stream()
                    .filter(o -> !game.getCommonObjectives().contains(o))
                    .collect(ArrayList::new, List::add, List::addAll);
            serverSubject.notify(player.getNickname(),
                    new ObjectivesMessage(Status.OBJECTIVES, secretObjectives, game.getCommonObjectives()));
        }
    }

    /**
     * Method that handles most of the phases of the game, making each player play his turn correctly.
     */
    private void startGame() {
        while (!game.isLastTurn() && !game.isGameStuck()) {
            for (Player player : game.getAllPlayers()) {
                serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
                if(player.isPlayerStuck()){
                    serverSubject.notifyAll(new Message(Status.NO_MOVES));
                    continue;
                }
                placeCard(player);
                drawCard(player);
            }
        }
        //last turn of the game
        for (Player player : game.getAllPlayers()) {
            if(game.isGameStuck()){
                break;
            }
            serverSubject.notifyAll(new Message(Status.LAST_TURN));
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            if(player.isPlayerStuck()){
                serverSubject.notifyAll(new Message(Status.NO_MOVES));
                continue;
            }
            placeCard(player);
        }
        //calculate the final score
        for (Player player : game.getAllPlayers()){
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            player.awardObjectivePoints();
        }
        List<String> winners = game.getWinningPlayers();
        serverSubject.notifyAll(new WinnersMessage(winners));
    }

    /**
     * Method that lets a player place a chosen card: it also checks if the placement is correct.
     * @param player the player that needs to place a card.
     */
    private void placeCard(Player player) {
        List<Corner> validPlacements = player.getAllValidCorners();
        serverSubject.notify(player.getNickname(),
                new ValidPlacementsMessage(Status.PLACE_CARD, player.getAllValidCards(), validPlacements));
        BasicCard cardToPlace = null;
        Corner chosenCorner = null;
        boolean moveValid = false;
        while (!moveValid) {
            LabeledMessage LabeledMessage = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (LabeledMessage.message() instanceof CardPlacementMessage cardPlacementMessage) {
                BasicCard cardToLookFor = cardPlacementMessage.getCard();
                cardToPlace = player.getAllValidCards().stream()
                        .filter(c -> c.equals(cardToLookFor))
                        .findFirst()
                        .orElse(null);
                chosenCorner = cardPlacementMessage.getCorner();
            }
            moveValid = isMoveValid(player, cardToPlace, chosenCorner);
            if (!moveValid){
                serverSubject.notify(player.getNickname(),
                        new ValidPlacementsMessage(Status.INVALID_PLACE_CARD, player.getAllValidCards(), validPlacements));
            }
        }
        player.placeCard(cardToPlace, chosenCorner);
    }

    /**
     * Method that checks if the player's chosen move is valid.
     *
     * @param player the player who's doing the move.
     * @param card the card chosen by the player.
     * @param corner the corner chosen by the player.
     * @return true if the move is valid, false if it isn't.
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
     * Method that lets a player draw a new card.
     * @param player the player that is drawing.
     */
    private void drawCard(Player player) {
        boolean drawSuccess = false;
        Status currentStatus = Status.DRAW;
        while (!drawSuccess){
            CardType typeChosen = null;
            int indexChosen = -1;
            do{
                serverSubject.notify(player.getNickname(), new DrawOptionsMessage(currentStatus, game.getDrawableCards()));
                currentStatus = Status.INVALID_DRAW;
                LabeledMessage LabeledMessage = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
                if (LabeledMessage.message() instanceof DrawChoiceMessage drawChoiceMessage){
                    indexChosen = drawChoiceMessage.getIndex();
                    typeChosen = drawChoiceMessage.getCardType();
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
     * A method that checks if a game is full.
     * @return true if it is, false if it isn't.
     */
    public boolean isGameFull() {
        return game.isGameFull();
    }

    /**
     * @return the game's name.
     */
    public String getName(){
        return name;
    }

    /**
     * A method that adds a message to the message queue.
     * @param message the message to add.
     * @param handler the handler that sent the message.
     */
    public synchronized void addMessageToQueue(Message message, NetworkHandler handler){
        messageQueue.add(new LabeledMessage(handler, message));
    }

    /**
     * A method that polls a message from the message queue; if it's a chat message, it sends it to the corresponding
     * recipients, and then polls another message. This method implements a timer.
     * @param handler the NetworkHandler from which the server expects a message.
     * @return the polled message.
     */
    public LabeledMessage readFromQueue(NetworkHandler handler){
        LabeledMessage labeledMessage = null;
        boolean isTimeOut = false;
        while(labeledMessage == null || isTimeOut){
            //TODO: TIMER IMPLEMENTATION
            synchronized(messageQueue){
                if(messageQueue.isEmpty()){
                    continue;
                }
                labeledMessage = messageQueue.poll();
                Message message = labeledMessage.message();
                //handle chat messages
                if(message.getStatus() == Status.CHAT && message instanceof ChatMessage chatMessage){
                    LabeledMessage finalLabeledMessage = labeledMessage;
                    String senderNickname = game.getAllPlayers().stream()
                            .map(Player::getNickname)
                            .filter(n -> serverSubject.getNetworkHandler(n) == finalLabeledMessage.networkHandler())
                            .findFirst().orElse("Missing Sender");
                    int chatMsgLength = chatMessage.getMessage().length();
                    List<String> recipients = chatMessage.getRecipients();
                    Message messageToSendBack = new ChatMessage(
                            chatMessage.getMessage().substring(0, Math.min(chatMsgLength, GameParameters.getMaxChatMessageLength())),
                            senderNickname,
                            chatMessage.getRecipients());
                    for(String nickname : recipients){
                        serverSubject.notify(nickname, messageToSendBack);
                    }
                    serverSubject.notify(senderNickname, messageToSendBack);
                }
                if(message.getStatus() == Status.CHAT || labeledMessage.networkHandler() != handler){
                    labeledMessage = null;
                }
            }
        }
        return labeledMessage;
    }

    /**
     * The main method of the class that calls all the above methods to correctly run a game. At the end of it,
     * it deletes the controller.
     */
    @Override
    public void run() {
        while(!game.isGameFull());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        System.out.println("A new game started");
        initializeGame();
        startGame();
        for (Player player : game.getAllPlayers()) {
            serverSubject.getNetworkHandler(player.getNickname()).setCurrentGame(null);
            serverSubject.unsubscribe(player.getNickname());
        }
        endGameProcedure.accept(this);
    }
}