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

import java.io.Serializable;
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
     * Constructor for the class
     *
     * @param numberOfPlayers the number of players allowed to enter the game and needed for it to start
     * @throws IllegalNumberOfPlayers when the number of players chosen exceeds the limit
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

    public List<Content> requestColors(){
        return new ArrayList<>(game.getAvailableColors());
    }

    /**
     * Method that adds a new player to the game, places his starter card and, if the game is full, it starts
     *
     * @param nickname the username of the new player
     * @throws GameException          when the color chosen by the new player was already taken
     * @throws GameFullException      when the game is full
     * @throws NicknameTakenException when the username of the new player already exists in the game
     */
    public void acceptPlayer(String nickname, Content color, NetworkHandler handler) throws GameFullException, NicknameTakenException, GameException{
        game.addPlayer(nickname, color);
        serverSubject.subscribe(nickname, handler);
    }

    /**
     * A method that lets every player place their starter card (by doing so, initializing the boards)
     */
    private void initializeGame() {
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, game.getDrawableCards()));
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
     * Core method that runs the match
     */
    private void startGame() {
        while (!game.isLastTurn()) {
            for (Player player : game.getAllPlayers()) {
                serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
                placeCard(player);
                drawCard(player);
            }
        }
        //last turn of the game
        for (Player player : game.getAllPlayers()) {
            serverSubject.notifyAll(new Message(Status.LAST_TURN));
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            placeCard(player);
        }
        //calculate the final score
        for (Player player : game.getAllPlayers()){
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            player.awardObjectivePoints();
        }
        List<String> winners = game.getWinningPlayers();
        serverSubject.notifyAll(new WinnersMessage(winners));
        //ViewUpdater is updated with the winners
    }

    /**
     * Method used to place a card for a player
     *
     * @param player the player that plays the card
     */
    private void placeCard(Player player) {
        serverSubject.notify(player.getNickname(),
                new ValidPlacementsMessage(Status.PLACE_CARD, player.getAllValidCards(), player.getAllValidCorners()));
        BasicCard cardToPlace = null;
        Corner chosenCorner = null;
        boolean moveValid = false;
        while (!moveValid) {
            LabeledMessage LabeledMessage = readFromQueue(serverSubject.getNetworkHandler(player.getNickname()));
            if (LabeledMessage.message() instanceof CardPlacementMessage cardPlacementMessage) {
                cardToPlace = cardPlacementMessage.getCard();
                chosenCorner = cardPlacementMessage.getCorner();
            }
            moveValid = isMoveValid(player,cardToPlace,chosenCorner);
            if (!moveValid){
                serverSubject.notify(player.getNickname(),
                        new ValidPlacementsMessage(Status.INVALID_PLACE_CARD, player.getAllValidCards(), player.getAllValidCorners()));
            }
        }
        player.placeCard(cardToPlace, chosenCorner);
    }

    /**
     * A method that checks if the player's chosen move is valid.
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
     * Method used to give the player a new card
     *
     * @param player the player that is drawing
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
     * A method that checks if the game is full.
     * @return true if it is, false if it isn't.
     */
    public boolean isGameFull() {
        return game.isGameFull();
    }

    /**
     * A getter method of game's name
     * @return the game's method
     */
    public String getName(){
        return name;
    }

    /**
     * A method that adds a message to the message queue.
     *
     * @param message the message to add.
     * @param handler the handler that sent the message.
     */
    public synchronized void addMessageToQueue(Message message, NetworkHandler handler){
        messageQueue.add(new LabeledMessage(handler, message));
    }

    /**
     * A method that polls a message for the message queue.
     * If it's a chat message, it sends it to the corresponding recipients, and then polls another message.
     * This method implements a timer.
     *
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
                    String senderNickname = game.getAllPlayers().stream()
                            .map(Player::getNickname)
                            .filter(n -> serverSubject.getNetworkHandler(n) == handler)
                            .findFirst().orElse("Missing Sender");
                    int chatMsgLength = chatMessage.getMessage().length();
                    List<String> recipients = chatMessage.getRecipients();
                    recipients.add(senderNickname);
                    for(String nickname : recipients){
                        serverSubject.notify(nickname, new ChatMessage(
                                chatMessage.getMessage().substring(0, Math.min(chatMsgLength, GameParameters.getMaxChatMessageLength())),
                                senderNickname,
                                chatMessage.getRecipients()));
                    }
                }
                if(message.getStatus() == Status.CHAT || labeledMessage.networkHandler() != handler){
                    labeledMessage = null;
                }
            }
        }
        return labeledMessage;
    }

    @Override
    public void run(){
        while(true){
            if(!game.isGameFull()){
                continue;
            }
            initializeGame();
            startGame();
            for(Player player : game.getAllPlayers()){
                game.getNetworkHandler(player.getNickname()).setCurrentGame(null);
                serverSubject.unsubscribe(player.getNickname());
            }
            endGameProcedure.accept(this);
        }
    }
}