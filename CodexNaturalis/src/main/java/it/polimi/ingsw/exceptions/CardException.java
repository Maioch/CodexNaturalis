package it.polimi.ingsw.exceptions;

/**
 * Unchecked exception class, thrown when either a GoldCard or a BasicCard is constructed with illegal parameters values.
 * Of course, this makes it impossible to construct an illegal instance of these classes.
 *
 * @see it.polimi.ingsw.model.shared.card.BasicCard
 * @see it.polimi.ingsw.model.shared.card.GoldCard
 */
public class CardException extends RuntimeException {
    /**
     * Constructor of the exception.
     *
     * @param message the message to be contained in the exception.
     */
    public CardException(String message){
        super(message);
    }
}