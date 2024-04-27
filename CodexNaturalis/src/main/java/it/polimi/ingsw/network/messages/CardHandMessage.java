package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.card.CardSides;

import java.util.ArrayList;

/**
 * Message sent by the client along with a list of card sides (e.g. everytime the server shows to the player his hand)
 */
public class CardHandMessage extends Message{
    private final ArrayList<CardSides> cardHand;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param CardHand the cards sent along the message
     */
    public CardHandMessage(Status status, ArrayList<CardSides> CardHand) {
        super(status);
        this.cardHand = CardHand;
    }

    /**
     * Getter method for the cards list
     * @return cardHand attribute
     */
    public ArrayList<CardSides> getCardHand() {
        return cardHand;
    }
}
