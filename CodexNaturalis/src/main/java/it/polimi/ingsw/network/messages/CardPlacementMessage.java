package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;

/**
 * Message sent by the client along with a card and a corner where he wants to place it
 */
public class CardPlacementMessage extends Message{
    private final BasicCard card;
    private final Corner corner;

    /**
     * Constructor for the class
     * @param card the card sent along the message
     * @param corner the corner sent along the message
     */
    public CardPlacementMessage(BasicCard card, Corner corner) {
        super(Status.PLACE_CARD);
        this.card = card;
        this.corner = corner;
    }

    /**
     * Getter method for the card
     * @return card attribute
     */
    public BasicCard getCard() {
        return card;
    }

    /**
     * Getter method for the corner
     * @return corner attribute
     */
    public Corner getCorner() {
        return corner;
    }
}
