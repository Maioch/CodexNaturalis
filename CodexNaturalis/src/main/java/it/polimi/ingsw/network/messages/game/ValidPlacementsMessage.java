package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message containing the player's placeable corners and cards.
 */
public class ValidPlacementsMessage extends Message {
    private final List<Corner> corners;
    private final List<BasicCard> cards;

    /**
     * Constructor for the message.
     * @param status status of the message.
     * @param cards placeable cards.
     * @param corners placeable corners.
     */
    public ValidPlacementsMessage(Status status, List<BasicCard> cards, List<Corner> corners) {
        super(status);
        this.corners = new ArrayList<>(corners);
        this.cards = new ArrayList<>(cards);
    }

    /**
     * @return a list of the placeable corners.
     */
    public List<Corner> getPlaceableCorners(){
        return new ArrayList<>(corners);
    }

    /**
     * @return a list of the placeable cards.
     */
    public List<BasicCard> getPlaceableCards(){
        return new ArrayList<>(cards);
    }
}