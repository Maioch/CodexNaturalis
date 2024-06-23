package it.polimi.ingsw.exceptions;

/**
 * Checked exception class, thrown when a TCP connection error occurs.
 * It is directly caught in the connection initializer which throws it.
 */
public class TCPException extends Exception{
    /**
     * Constructor of the exception.
     */
    public TCPException(){ super(); }

    /**
     * Gets the message contained in the exception.
     *
     * @return always the same message (not dynamically chose).
     */
    @Override
    public String getMessage(){
        return "couldn't connect to the server";
    }
}
