package it.polimi.ingsw.exceptions;

public class TCPException extends Exception{
    public TCPException(){ super(); }

    @Override
    public String getMessage(){
        return "couldn't connect to the server";
    }
}
