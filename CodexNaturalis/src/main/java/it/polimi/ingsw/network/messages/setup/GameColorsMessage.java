package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent along with a content value (e.g. the color chosen by the player).
 */
public class GameColorsMessage extends Message {
    private final List<Content> content;
    private final int gameId;

    /**
     * Constructor for the class.
     * @param status the message sent.
     * @param content the content sent along the message.
     */
    public GameColorsMessage(Status status, List<Content> content, int gameId) {
        super(status);
        this.content = new ArrayList<>(content);
        this.gameId = gameId;
    }

    /**
     * @return the attached content.
     */
    public List<Content> getContent() {
        return new ArrayList<>(content);
    }

    /**
     * @return the associated game id.
     */
    public int getGameId(){
        return gameId;
    }
}