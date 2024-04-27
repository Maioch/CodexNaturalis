package it.polimi.ingsw.network.messages;

/**
 * Message sent by the client along with a string value (e.g. the username)
 */
public class StringMessage extends Message{
    private final String string;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param string the string sent along the message
     */
    public StringMessage(Status status, String string) {
        super(status);
        this.string = string;
    }

    /**
     * Getter method for the string sent along the message
     * @return string attribute
     */
    public String getString() {
        return string;
    }
}
