package it.polimi.ingsw.network.shared.messages.generic;

import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

/**
 * Message sent along with a string value (e.g. the username).
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class StringMessage extends Message {
    private final String string;

    /**
     * Constructor for the class.
     *
     * @param status the message sent.
     * @param string the string sent along the message.
     */
    public StringMessage(Status status, String string) {
        super(status);
        this.string = string;
    }

    /**
     * Gets the attached string.
     *
     * @return the attached string.
     */
    public String getString() {
        return string;
    }
}