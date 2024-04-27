package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.Content;

/**
 * Message sent by the client along with a content value (e.g. the color chosen by the player)
 */
public class ContentMessage extends Message{
    private final Content content;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param content the content sent along the message
     */
    public ContentMessage(Status status,Content content) {
        super(status);
        this.content = content;
    }

    /**
     * Getter method for the content sent along the message
     * @return content attribute
     */
    public Content getContent() {
        return content;
    }
}
