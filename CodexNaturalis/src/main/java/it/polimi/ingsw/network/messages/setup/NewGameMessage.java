package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

public class NewGameMessage extends Message {
    private final String name;
    private final int numberOfPlayers;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param name of the game, decided by the player creating the match
     * @param numberOfPlayers integer representing the game's number of players
     */
    public NewGameMessage(Status status, String name, int numberOfPlayers) {
        super(status);
        this.name = name;
        this.numberOfPlayers =numberOfPlayers;
    }

    /**
     * Getter method for the string sent along the message
     * @return string attribute
     */
    public String getName() {
        return name;
    }

    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }
}
