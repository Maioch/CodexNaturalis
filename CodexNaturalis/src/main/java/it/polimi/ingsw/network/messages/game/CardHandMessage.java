package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent for a player's hand cards request.
 */
public class CardHandMessage extends Message {
    private final List<CardSides> cardHand;

    /**
     * Constructor for the class.
     * @param status the message sent.
     * @param CardHand the cards sent along the message.
     */
    public CardHandMessage(Status status, List<CardSides> CardHand) {
        super(status);
        this.cardHand = new ArrayList<>(CardHand);
    }

    /**
     * @return the player's hand cards.
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