package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a user with the same username as one of the players
 * who have already joined the game tries to join it.
 */
public class UsernameTakenException extends RuntimeException{
    public UsernameTakenException(){

    }

    @Override
    public String getMessage(){
        return "The chosen username has already been taken";
    }
}
