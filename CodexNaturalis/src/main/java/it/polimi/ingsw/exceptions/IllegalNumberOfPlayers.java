package it.polimi.ingsw.exceptions;

public class IllegalNumberOfPlayers extends Exception{
    public IllegalNumberOfPlayers(){
    }

    @Override
    public String getMessage(){
        return "The game has an illegal number of players (either not enough players or too much players)";
    }
}
