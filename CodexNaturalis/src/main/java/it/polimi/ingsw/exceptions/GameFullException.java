package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a user tries to join a room which has already reached its max capacity
 */
public class GameFullException extends RuntimeException{
    public GameFullException(){

    }

    @Override
    public String getMessage() {
        return "The game you tried to join is already full";
    }
}

