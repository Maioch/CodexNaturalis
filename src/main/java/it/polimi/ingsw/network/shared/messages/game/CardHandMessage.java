package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent for a player's hand cards request.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CardHandMessage extends Message {
    private final List<CardSides> cardHand;

    /**
     * Class constructor.
     *
     * @param status   the message status.
     * @param CardHand the cards sent along the message.
     *
     * @see Status
     * @see CardSides
     */
    public CardHandMessage(Status status, List<CardSides> CardHand) {
        super(status);
        this.cardHand = new ArrayList<>(CardHand);
    }

    /**
     * Gets the attached hand's cards.
     *
     * @return the attached hand's cards.
     *
     * @see CardSides
     */
    public List<CardSides> getCardHand() {
        return new ArrayList<>(){{
            for(CardSides cardSides : cardHand){
                add(new CardSides(
                        cardSides.frontSide() != null ? cardSides.frontSide().copy() : null,
                        cardSides.backSide() != null ? cardSides.backSide().copy() : null));
            }
        }};
    }
}