package it.polimi.ingsw.network.shared.messages.setup;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent along with a content value (e.g. the color chosen by the player).
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class GameColorsMessage extends Message {
    private final List<Content> content;
    private final int gameId;

    /**
     * Constructor for the class.
     *
     * @param status  the message sent.
     * @param content the content sent along the message.
     * @param gameId  the game's id.
     *
     * @see Status
     * @see Content
     */
    public GameColorsMessage(Status status, List<Content> content, int gameId) {
        super(status);
        this.content = new ArrayList<>(content);
        this.gameId = gameId;
    }

    /**
     * Gets the attached content.
     *
     * @return the attached content.
     *
     * @see Content
     */
    public List<Content> getContent() {
        return new ArrayList<>(content);
    }

    /**
     * Gets the attached game id.
     *
     * @return the attached game id.
     */
    public int getGameId(){
        return gameId;
    }
}