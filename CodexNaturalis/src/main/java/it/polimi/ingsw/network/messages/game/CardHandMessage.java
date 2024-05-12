package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the client along with a list of card sides (e.g. everytime the server shows to the player his hand)
 */
public class CardHandMessage extends Message {
    private final List<CardSides> cardHand;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param CardHand the cards sent along the message
     */
    public CardHandMessage(Status status, List<CardSides> CardHand) {
        super(status);
        this.cardHand = new ArrayList<>(CardHand);
    }

    /**
     * Getter method for the cards list
     * @return cardHand attribute
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
