package it.polimi.ingsw.model.exceptions;

/**
 * Exception thrown when there's an illegal number of player in the game (either less than 2 or more than 4)
 */

public class IllegalNumberOfPlayers extends Exception{
    public IllegalNumberOfPlayers(){

    }

    @Override
    public String getMessage(){
        return "The game has an illegal number of players (either not enough players or too much players)";
    }
}
