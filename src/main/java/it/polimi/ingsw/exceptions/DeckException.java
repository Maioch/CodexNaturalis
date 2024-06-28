package it.polimi.ingsw.exceptions;

/**
 * Unchecked exception class, thrown when either a Deck or a TurnDeck is constructed with illegal parameters values.
 * Of course, this makes it impossible to construct an illegal instance of these classes.
 *
 * @see it.polimi.ingsw.model.server.deck.Deck
 * @see it.polimi.ingsw.model.server.deck.TurnDeck
 */
public class DeckException extends RuntimeException {

    /**
     * Constructor of the exception.
     *
     * @param message the message to be contained in the exception.
     */
    public DeckException(String message){
        super(message);
    }
}