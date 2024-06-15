package it.polimi.ingsw.exceptions;

public class NicknameException extends Exception{
    public NicknameException(){}

    @Override
    public String getMessage() {
        return "The chosen nickname is invalid";
    }
}