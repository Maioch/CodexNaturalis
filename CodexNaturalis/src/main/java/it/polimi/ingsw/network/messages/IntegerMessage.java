package it.polimi.ingsw.network.messages;

/**
 * Message sent by the client along with an integer value (e.g. the number of players of a new match)
 */
public class IntegerMessage extends Message {
    private final int value;

    /**
     *Constructor for the class
     * @param status the message sent
     * @param value the integer value sent along the message
     */
    public IntegerMessage(Status status, int value){
        super(status);
        this.value = value;
    }

    /**
     * Getter method for the integer sent along the message
     * @return value attribute
     */
    public int getValue() {
        return value;
    }
}
