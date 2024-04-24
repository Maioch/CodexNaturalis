package it.polimi.ingsw.exceptions;

public class GameFullException extends Exception{
    public GameFullException(){}

    @Override
    public String getMessage(){
        return "The chosen game is full";
    }
}