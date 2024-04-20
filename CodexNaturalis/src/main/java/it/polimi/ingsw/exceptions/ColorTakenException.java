package it.polimi.ingsw.exceptions;

/**
 * Exception thrown when a user selects an already taken color
 */
public class ColorTakenException extends RuntimeException{
    public ColorTakenException(){}

    @Override
    public String getMessage(){
        return "The chosen color has already been taken";
    }
}
