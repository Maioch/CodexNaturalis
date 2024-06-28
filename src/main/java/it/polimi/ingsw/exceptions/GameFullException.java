package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when there's a user tries to join an already full game.
 */
public class GameFullException extends Exception{

    /**
     * Constructor of the exception.
     */
    public GameFullException(){}

    /**
     * Gets the message contained in the exception.
     *
     * @return always the same message (not dynamically chosen).
     */
    @Override
    public String getMessage(){
        return "The chosen game is full";
    }
}