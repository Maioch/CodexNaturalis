package it.polimi.ingsw.exceptions;

public class NicknameTakenException extends Exception{
    public NicknameTakenException(){}

    @Override
    public String getMessage() {
        return "the chosen nickname is taken";
    }
}
