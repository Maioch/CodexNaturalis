package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message containing the player's placeable corners and cards.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ValidPlacementsMessage extends Message {
    private final List<Corner> corners;
    private final List<BasicCard> cards;

    /**
     * Constructor for the message.
     *
     * @param status  status of the message.
     * @param cards   placeable cards.
     * @param corners placeable corners.
     *
     * @see Status
     * @see BasicCard
     * @see Corner
     */
    public ValidPlacementsMessage(Status status, List<BasicCard> cards, List<Corner> corners) {
        super(status);
        this.corners = new ArrayList<>(corners);
        this.cards = new ArrayList<>(cards);
    }

    /**
     * Gets the attached list of the placeable corners.
     *
     * @return the attached list of the placeable corners.
     *
     * @see Corner
     */
    public List<Corner> getPlaceableCorners(){
        return new ArrayList<>(corners);
    }

    /**
     * Gets the attached list of the placeable cards.
     *
     * @return the attached list of the placeable cards.
     *
     * @see BasicCard
     */
    public List<BasicCard> getPlaceableCards(){
        return new ArrayList<>(cards);
    }
}