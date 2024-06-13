package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * Message sent for a card placement request.
 */
public class CardPlacementMessage extends Message {
    private final BasicCard card;
    private final Corner corner;

    /**
     * Constructor for the class.
     * @param card the card sent along the message.
     * @param corner the corner sent along the message.
     */
    public CardPlacementMessage(BasicCard card, Corner corner) {
        super(Status.PLACE_CARD);
        this.card = card;
        this.corner = corner;
    }

    /**
     * @return the card to be placed.
     */
    public BasicCard getCard() {
        return card;
    }

    /**
     * @return the corner where the card should be placed.
     */
    public Corner getCorner() {
        return corner;
    }
}