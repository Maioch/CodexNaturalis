package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.Listener;
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
import it.polimi.ingsw.utilities.Pair;

import java.util.*;
import java.util.List;

/**
 * Class that represents the controller for each game, according to the MVC model.
 *
 * @author Andrea Fidanza, Guglielmo Gatti, Marco Maiocchi, Francesco Saverio Nisoli
 */
public class GameController implements Runnable{
    private final GameModel game;
    private final String name;
    private final ServerSubject serverSubject;
    private final Queue<Pair<Listener,Message>> messageQueue;
    private boolean isEnded;

    /**
     * Constructor for the class
     *
     * @param numberOfPlayers the number of players allowed to enter the game and needed for it to start
     * @throws IllegalNumberOfPlayers when the number of players chosen exceeds the limit
     */
    public GameController(int numberOfPlayers, ServerSubject serverSubject, String name) throws IllegalNumberOfPlayers {
        this.game = new GameModel(numberOfPlayers, serverSubject);
        this.name = name;
        this.serverSubject = serverSubject;
        this.messageQueue = new LinkedList<>();
        this.isEnded = false;
    }

    public ArrayList<Content> requestColors(){
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
    public void acceptPlayer(String nickname, Content color, Listener serverListener) throws GameFullException, NicknameTakenException, GameException{
        game.addPlayer(nickname, color);
        serverSubject.subscribe(nickname, serverListener);
    }

    /**
     * A method that lets every player place their starter card (by doing so, initializing the boards)
     */
    private void initializeGame() {
        serverSubject.notifyAll(new DrawOptionsMessage(game.getDrawableCards()));
        for (Player player : game.getAllPlayers()) {
            serverSubject.notifyAll(new StringMessage(Status.TURN_NOTIFICATION, player.getNickname()));
            CardSides starterCard = player.getHandCards().getFirst();
            serverSubject.notify(player.getNickname(), new CardHandMessage(Status.GAME_STARTED, player.getHandCards()));
            BasicCard starterSide = null;
            while (!starterCard.frontSide().equals(starterSide) && !starterCard.backSide().equals(starterSide)){
                Pair<Listener, Message> messagePair = readFromQueue(serverSubject.getListener(player.getNickname()));
                if (messagePair.getValue() instanceof CardPlacementMessage cardPlacementMessage){
                    starterSide = cardPlacementMessage.getCard();
                }
            }
            player.placeStarterCard(starterSide);
            ArrayList<Objective> secretObjectives = player.getObjectives().stream()
                    .filter(o -> !game.getCommonObjectives().contains(o))
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            serverSubject.notify(player.getNickname(),new ObjectivesMessage(Status.SEND_OBJECTIVES,secretObjectives,game.getCommonObjectives()));
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
        isEnded = true;
        //ViewUpdater is updated with the winners
    }

    /**
     * Method used to place a card for a player
     *
     * @param player the player that plays the card
     */
    private void placeCard(Player player) {
        serverSubject.notify(player.getNickname(), new Message(Status.PLACE_CARD));
        BasicCard cardToPlace = null;
        Corner chosenCorner = null;
        boolean moveValid = false;
        while (!moveValid) {
            Pair<Listener, Message> messagePair = readFromQueue(serverSubject.getListener(player.getNickname()));
            if (messagePair.getValue() instanceof CardPlacementMessage cardPlacementMessage) {
                cardToPlace = cardPlacementMessage.getCard();
                chosenCorner = cardPlacementMessage.getCorner();
            }
            moveValid = isMoveValid(player,cardToPlace,chosenCorner);
            if (!moveValid){
                serverSubject.notify(player.getNickname(), new Message(Status.PLACEMENT_FAILED));
            }
        }
        player.placeCard(cardToPlace, chosenCorner);
    }

    private boolean isMoveValid(Player player,BasicCard card, Corner corner){
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
        while (!drawSuccess){
            serverSubject.notify(player.getNickname(),new Message(Status.DRAW));
            CardType typeChosen = null;
            int indexChosen = -1;
            do{
                Pair<Listener,Message> messagePair = readFromQueue(serverSubject.getListener(player.getNickname()));
                if (messagePair.getValue() instanceof DrawChoiceMessage drawChoiceMessage){
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
        serverSubject.notifyAll(new DrawOptionsMessage(game.getDrawableCards()));
    }

    public boolean isGameEnded(){
        return isEnded;
    }

    public String getName(){
        return name;
    }

    public synchronized void addMessageToQueue(Message message, Listener listener){
        messageQueue.add(new Pair<>(listener,message));
    }

    public Pair<Listener,Message> readFromQueue(Listener serverListener){
        Pair<Listener,Message> messagePair = null;
        boolean isTimeOut = false; //must implement a timer system
        while(messagePair == null || isTimeOut){
            synchronized(messageQueue){
                if(messageQueue.isEmpty()){
                    continue;
                }
                messagePair = messageQueue.peek().getKey() == serverListener ? messageQueue.poll() : null;
            }
        }
        return messagePair;
    }

    @Override
    public void run(){
        while(true){
            if(!game.isGameFull()){
                continue;
            }
            initializeGame();
            startGame();
        }
    }
}