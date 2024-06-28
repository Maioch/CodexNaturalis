package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when a TCP error occurs.
 */
public class TCPException extends Exception{

    /**
     * Constructor of the exception.
     */
    public TCPException(){
        super();
    }

    /**
     * Gets the message contained in the exception.
     *
     * @return always the same message (not dynamically chosen).
     */
    @Override
    public String getMessage(){
        return "couldn't connect to the server";
    }
}