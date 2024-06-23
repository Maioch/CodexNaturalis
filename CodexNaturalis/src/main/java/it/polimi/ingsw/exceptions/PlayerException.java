package it.polimi.ingsw.exceptions;

/**
 * Unchecked exception class, thrown when a player tries to place a starter card while there already is one in their board.
 * This makes such case is severe.
 */
public class PlayerException extends RuntimeException {
    /**
     * Constructor of the exception.
     *
     * @param message the message to be contained in the exception.
     */
    public PlayerException(String message){
        super(message);
    }
}