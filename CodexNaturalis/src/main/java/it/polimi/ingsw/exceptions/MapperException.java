package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when the conversion of type in read from input (string to a chosen one)
 * doesn't happen correctly.
 */
public class MapperException extends Exception{

    /**
     * Constructor of the exception.
     */
    public MapperException(){
        super();
    }
}