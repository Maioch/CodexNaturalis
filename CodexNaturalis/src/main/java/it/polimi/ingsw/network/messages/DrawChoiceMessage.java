package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.model.server.card.CardType;

/**
 * A message sent by the client, containing the infos regarding the draw choice
 */
public class DrawChoiceMessage extends Message{
    private final int index;
    private final CardType cardType;

    /**
     * A constructor of the class
     * @param index integer representing which of the deck's drawable cars is chosen
     * @param cardType the chosen card deck type
     */
    public DrawChoiceMessage(int index, CardType cardType) {
        super(Status.DRAW);
        this.index = index;
        this.cardType = cardType;
    }

    /**
     * Getter of the index
     * @return the index of the chosen card
     */
    public int getIndex() {
        return index;
    }

    /**
     * Getter of the card type
     * @return the card type of the chosen card
     */
    public CardType getCardType() {
        return cardType;
    }
}
