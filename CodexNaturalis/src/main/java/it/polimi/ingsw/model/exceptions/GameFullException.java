package it.polimi.ingsw.model.exceptions;

/**
 * Exception thrown when a user tries to join a room which has already reached its max capacity
 */
public class GameFullException extends Exception{
    public GameFullException(){

    }

    @Override
    public String getMessage() {
        return "The room you tried to join is already full";
    }
}

