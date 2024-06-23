package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when the conversion of type in read from input (string to a chosen one)
 * doesn't happen correctly.
 * In general, this case is not severe and will be caught (and managed).
 */
public class MapperException extends Exception{
    /**
     * Constructor of the exception.
     */
    public MapperException(){
        super();
    }
}