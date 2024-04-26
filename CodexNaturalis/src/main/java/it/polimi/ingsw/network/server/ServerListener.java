package it.polimi.ingsw.network.server;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.card.BasicCard;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.CardType;
import it.polimi.ingsw.model.card.Objective;
import it.polimi.ingsw.model.card.corner.Corner;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public interface ServerListener {

    /**
     * Method that notifies the player that they have to choose their color
     * @return the color chosen by the player
     */
    Content requestColor();

    /**
     * Method that notifies the player that they have to choose a card to place
     * @return the card chosen by the player
     */
    BasicCard requestCardToPlace();

    /**
     * Method that notifies the player that they have to choose the corner on which the card will be placed
     * @return the corner chosen by the player
     */
    Corner requestCornerToPlaceOn();

    /**
     * Method that notifies the player that they have to choose the side on which to place their starter card.
     * @return the starter card's side chosen by the player
     */
    BasicCard requestStarterSide();

    /**
     * Method that notifies the player that they have to choose the card they want to draw
     * @return the chosen card's coordinates (point-formed: x-coordinate representing the chosen deck, y-coordinate representing the visible card's index)
     */
    Point requestCardToDraw();

    /**
     * Method that sends to the player their objectives, both common and personal
     * @param objectives all the player's objectives
     */
    void sendObjectives(ArrayList<Objective> objectives);

    /**
     * Method that sends to the player their hand cards
     * @param handCards tha cards the player has in his hand
     */
    void sendHandCards(ArrayList<CardSides> handCards);

    /**
     * Method that sends a player's board to everyone in the game
     * @param board the player's current board
     */
    void sendBoard(ArrayList<BasicCard> board);

    /**
     * Method that sends to the player the starter card chosen for him
     * @param starter the player's starter card
     */
    void sendStarterCard(CardSides starter);

    /**
     * Method that sends to the player a HashMap containing all the currently drawable cards.
     * @param drawableCards all the possible draw options
     */
    void sendDrawableCards(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
}