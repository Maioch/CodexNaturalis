package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.network.server.ServerSubject;
import it.polimi.ingsw.server.model.Content;
import it.polimi.ingsw.server.model.GameModel;
import it.polimi.ingsw.server.model.GameParameters;
import it.polimi.ingsw.server.model.Player;
import it.polimi.ingsw.server.model.card.BasicCard;
import it.polimi.ingsw.server.model.card.CardSides;
import it.polimi.ingsw.server.model.card.CardType;
import it.polimi.ingsw.server.model.card.corner.Corner;
import it.polimi.ingsw.network.server.DeprecatedServerListener;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents the controller for each game, according to the MVC model.
 *
 * @author Andrea Fidanza, Guglielmo Gatti, Marco Maiocchi, Francesco Saverio Nisoli
 */
public class GameController extends ServerSubject{
    private final GameModel game;
    private final HashMap<String, DeprecatedServerListener> listeners;

    /**
     * Constructor for the class
     *
     * @param firstPlayerName the player that creates a game and the first to be added
     * @param numberOfPlayers the number of players allowed to enter the game and needed for it to start
     * @throws IllegalNumberOfPlayers when the number of players chosen exceeds the limit
     */
    public GameController(String firstPlayerName, DeprecatedServerListener firstPlayerListener, int numberOfPlayers) throws IllegalNumberOfPlayers {
        this.game = new GameModel(numberOfPlayers);
        this.listeners = new HashMap<>();
        try {
            acceptPlayer(firstPlayerName, firstPlayerListener);
        } catch (GameException | GameFullException | NicknameTakenException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Method that adds a new player to the game, places his starter card and, if the game is full, it starts
     *
     * @param nickname the username of the new player
     * @throws GameException          when the color chosen by the new player was already taken
     * @throws GameFullException      when the game is full
     * @throws NicknameTakenException when the username of the new player already exists in the game
     */
    public void acceptPlayer(String nickname, DeprecatedServerListener listener) throws GameException, GameFullException, NicknameTakenException {
        listeners.put(nickname, listener);
        Content chosenColor = listener.requestColor();
        game.addPlayer(nickname, chosenColor);
        if (game.isGameFull()) {
            initializeGame();
            startGame();
        }
    }

    /**
     * A method that lets every player place their starter card (by doing so, initializing the boards)
     */
    private void initializeGame() {
        for (Player player : game.getAllPlayers()) {
            DeprecatedServerListener currentListener = listeners.get(player.getNickname());
            CardSides starterCard = player.getHandCards().getFirst();
            currentListener.sendStarterCard(starterCard);
            BasicCard starterSide = currentListener.requestStarterSide();
            while (!starterCard.frontSide().equals(starterSide) && !starterCard.backSide().equals(starterSide)){
                starterSide = currentListener.requestStarterSide();
            }
            player.placeStarterCard(starterSide);
            currentListener.sendObjectives(player.getObjectives());
        }
    }

    /**
     * Core method that runs the match
     */
    private void startGame() {
        while (!game.isLastTurn()) {
            for (Player player : game.getAllPlayers()) {
                DeprecatedServerListener currentListener = listeners.get(player.getNickname());
                currentListener.sendHandCards(player.getHandCards());
                currentListener.sendBoard(player.getPlacedCards());
                placeCard(player);
                drawCard(player);
            }
        }
        //last turn of the game
        for (Player player : game.getAllPlayers()) {
            DeprecatedServerListener currentListener = listeners.get(player.getNickname());
            currentListener.sendHandCards(player.getHandCards());
            currentListener.sendBoard(player.getPlacedCards());
            placeCard(player);
        }
        List<String> winners = game.getWinningPlayers();
        //ViewUpdater is updated with the winners
    }

    /**
     * Method used to place a card for a player
     *
     * @param player the player that plays the card
     */
    private void placeCard(Player player) {
        DeprecatedServerListener listener = listeners.get(player.getNickname());
        BasicCard cardToPlace = listener.requestCardToPlace();
        while (!player.checkRequirements(cardToPlace) || !player.isCardInHand(cardToPlace)) {
            cardToPlace = listener.requestCardToPlace();
        }
        Corner chosenCorner = listener.requestCornerToPlaceOn();
        while (!player.checkIfPlaceable(chosenCorner) || !player.isCornerPartOfBoard(chosenCorner)) {
            chosenCorner = listener.requestCornerToPlaceOn();
        }
        player.placeCard(cardToPlace, chosenCorner);
    }

    /**
     * Method used to give the player a new card
     *
     * @param player the player that is drawing
     */
    private void drawCard(Player player) {
        DeprecatedServerListener listener = listeners.get(player.getNickname());
        listener.sendDrawableCards(game.getDrawableCards());
        boolean drawSuccess = false;
        while (!drawSuccess){
            Point cardChosen;
            CardType typeChosen;
            int indexChosen;
            do{
                cardChosen = listener.requestCardToDraw();
                Point finalCardChosen = cardChosen;
                typeChosen = Arrays.stream(CardType.values()).filter(c -> c.ordinal() == finalCardChosen.x).findFirst().orElse(null);
                indexChosen = cardChosen.y;
            }while(typeChosen == null || indexChosen < 0 || indexChosen > GameParameters.getNumberOfVisibleCards());
            try{
                drawSuccess = true;
                game.drawCard(player, typeChosen, indexChosen);
            }catch (GameException e) {
                drawSuccess = false;
            }
        }
    }
}