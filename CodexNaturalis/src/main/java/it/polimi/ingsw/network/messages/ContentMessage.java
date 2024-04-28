package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.Content;

import java.util.ArrayList;

/**
 * Message sent by the client along with a content value (e.g. the color chosen by the player)
 */
public class ContentMessage extends Message{
    private final ArrayList<Content> content;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param content the content sent along the message
     */
    public ContentMessage(Status status, ArrayList<Content> content) {
        super(status);
        this.content = content;
    }

    /**
     * Getter method for the contents sent along the message
     * @return content attribute
     */
    public ArrayList<Content> getContent() {
        return new ArrayList<>(content);
    }
}
