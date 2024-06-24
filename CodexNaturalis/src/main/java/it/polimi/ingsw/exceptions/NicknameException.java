package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when there's a user tries to join a game with an invalid nickname.
 */
public class NicknameException extends Exception{

    /**
     * Constructor of the exception.
     */
    public NicknameException(){}

    /**
     * Gets the message contained in the exception.
     *
     * @return always the same message (not dynamically chosen).
     */
    @Override
    public String getMessage() {
        return "The chosen nickname is invalid";
    }
}