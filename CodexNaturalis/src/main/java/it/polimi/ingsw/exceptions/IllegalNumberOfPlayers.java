package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when there's a user tries to create a game with too many players.
 * In general, this case is not severe and will be caught (and managed).
 */
public class IllegalNumberOfPlayers extends Exception{

    /**
     * Constructor of the exception.
     */
    public IllegalNumberOfPlayers(){}

    /**
     * Gets the message contained in the exception.
     *
     * @return always the same message (not dynamically chose).
     */
    @Override
    public String getMessage(){
        return "The game has an illegal number of players (either not enough players or too much players)";
    }
}