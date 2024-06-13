package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * Message used to handle a new game creation.
 */
public class NewGameMessage extends Message {
    private final String name;
    private final int numberOfPlayers;

    /**
     * Constructor for the class.
     * @param name of the game, decided by the player creating the match.
     * @param numberOfPlayers integer representing the game's number of players.
     */
    public NewGameMessage(String name, int numberOfPlayers) {
        super(Status.NEW_GAME);
        this.name = name;
        this.numberOfPlayers = numberOfPlayers;
    }

    /**
     * @return the name of the game.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the maximum amount of players for the game.
     */
    public int getNumberOfPlayers(){
        return numberOfPlayers;
    }
}