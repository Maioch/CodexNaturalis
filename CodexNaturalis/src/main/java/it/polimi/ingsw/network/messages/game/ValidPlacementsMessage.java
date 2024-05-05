package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;

/**
 * A message containing the player's placeable corners and cards
 */
public class ValidPlacementsMessage extends Message {
    ArrayList<Corner> corners;
    ArrayList<BasicCard> cards;

    /**
     * Constructor of the message
     * @param status status of the message
     * @param cards placeable cards
     * @param corners placeable corners
     */
    public ValidPlacementsMessage(Status status, ArrayList<BasicCard> cards, ArrayList<Corner> corners) {
        super(status);
        this.corners = corners;
        this.cards = cards;
    }

    /**
     * Getter of the placeable corners
     * @return a list of the placeable corners
     */
    public ArrayList<Corner> getPlaceableCorners(){
        return new ArrayList<>(corners);
    }

    /**
     * Getter of the placeable cards
     * @return a list of the placeable cards
     */
    public ArrayList<BasicCard> getPlaceableCards(){
        return new ArrayList<>(cards);
    }
}