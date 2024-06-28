package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

/**
 * Message sent during the draw phase of the game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class DrawChoiceMessage extends Message {
    private final int index;
    private final CardType cardType;

    /**
     * Constructor for the class.
     *
     * @param index    integer representing which of the deck's drawable cars is chosen.
     * @param cardType the chosen card deck type.
     *
     * @see CardType
     */
    public DrawChoiceMessage(int index, CardType cardType) {
        super(Status.DRAW);
        this.index = index;
        this.cardType = cardType;
    }

    /**
     * Gets the attached index of the chosen card.
     *
     * @return the attached index of the chosen card.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Gets the attached card type of the chosen card.
     *
     * @return the attached card type of the chosen card.
     *
     * @see CardType
     */
    public CardType getCardType() {
        return cardType;
    }
}