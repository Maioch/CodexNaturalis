package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent by the client along with a content value (e.g. the color chosen by the player)
 */
public class GameColorsMessage extends Message {
    private final List<Content> content;
    private final int gameId;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param content the content sent along the message
     */
    public GameColorsMessage(Status status, List<Content> content, int gameId) {
        super(status);
        this.content = new ArrayList<>(content);
        this.gameId = gameId;
    }

    /**
     * Getter method for the contents sent along the message
     * @return content attribute
     */
    public List<Content> getContent() {
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
