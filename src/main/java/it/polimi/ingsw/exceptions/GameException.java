package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when there's an anomaly occurred during a GameModel update.
 */
public class GameException extends Exception {

    /**
     * Constructor of the exception.
     *
     * @param message the message to be contained in the exception.
     */
    public GameException(String message){
        super(message);
    }
}