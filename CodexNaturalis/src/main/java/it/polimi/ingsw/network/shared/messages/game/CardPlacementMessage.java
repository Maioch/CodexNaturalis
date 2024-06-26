package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

/**
 * Message sent for a card placement request.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CardPlacementMessage extends Message {
    private final BasicCard card;
    private final Corner corner;

    /**
     * Constructor for the class.
     *
     * @param card   the card sent along the message.
     * @param corner the corner sent along the message.
     *
     * @see BasicCard
     * @see Corner
     */
    public CardPlacementMessage(BasicCard card, Corner corner) {
        super(Status.PLACE_CARD);
        this.card = card;
        this.corner = corner;
    }

    /**
     * Gets the attached card to be placed.
     *
     * @return attached the card to be placed.
     *
     * @see BasicCard
     */
    public BasicCard getCard() {
        return card;
    }

    /**
     * Gets the attached corner where the card should be placed.
     *
     * @return the attached corner where the card should be placed.
     *
     * @see Corner
     */
    public Corner getCorner() {
        return corner;
    }
}