package it.polimi.ingsw.network.messages;

import java.io.Serializable;

/**
 * Default message used when there's no need to notify the client with an object, for example the cards in his hand.
 */
public class Message implements Serializable{
    protected final Status status;

    /**
     * Constructor for the class
     * @param status the message sent
     */
    public Message(Status status){
        this.status = status;
    }

    /**
     * Getter for the message
     * @return the status attribute
     */
    public Status getStatus(){
        return status;
    }
}
