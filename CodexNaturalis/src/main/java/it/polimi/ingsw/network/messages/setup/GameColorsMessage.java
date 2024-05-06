package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;

/**
 * Message sent by the client along with a content value (e.g. the color chosen by the player)
 */
public class GameColorsMessage extends Message {
    private final ArrayList<Content> content;
    private final int gameId;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param content the content sent along the message
     */
    public GameColorsMessage(Status status, ArrayList<Content> content, int gameId) {
        super(status);
        this.content = content;
        this.gameId = gameId;
    }

    /**
     * Getter method for the contents sent along the message
     * @return content attribute
     */
    public ArrayList<Content> getContent() {
        return new ArrayList<>(content);
    }

    /**
     * Getter method for the game id sent along the message
     * @return gameID attribute
     */
    public int getGameId(){
        return gameId;
    }
}
