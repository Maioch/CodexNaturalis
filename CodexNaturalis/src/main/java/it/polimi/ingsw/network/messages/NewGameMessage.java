package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.Content;

public class NewGameMessage extends Message{
    private final String name;
    private final int numberOfPlayers;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param name string sent along the message
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
