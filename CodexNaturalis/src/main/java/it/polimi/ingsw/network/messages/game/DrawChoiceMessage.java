package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * Message sent during the draw phase of the game.
 */
public class DrawChoiceMessage extends Message {
    private final int index;
    private final CardType cardType;

    /**
     * Constructor for the class.
     * @param index integer representing which of the deck's drawable cars is chosen.
     * @param cardType the chosen card deck type.
     */
    public DrawChoiceMessage(int index, CardType cardType) {
        super(Status.DRAW);
        this.index = index;
        this.cardType = cardType;
    }

    /**
     * @return the index of the chosen card.
     */
    public int getIndex() {
        return index;
    }

    /**
     * @return the card type of the chosen card.
     */
    public CardType getCardType() {
        return cardType;
    }
}
