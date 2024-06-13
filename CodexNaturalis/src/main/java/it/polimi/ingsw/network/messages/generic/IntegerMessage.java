package it.polimi.ingsw.network.messages.generic;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * Message sent along with an integer value (e.g. the number of players of a new match).
 */
public class IntegerMessage extends Message {
    private final int value;

    /**
     * Constructor for the class.
     * @param status the message sent.
     * @param value the integer value sent along the message.
     */
    public IntegerMessage(Status status, int value){
        super(status);
        this.value = value;
    }

    /**
     * @return the attached integer.
     */
    public int getValue() {
        return value;
    }
}