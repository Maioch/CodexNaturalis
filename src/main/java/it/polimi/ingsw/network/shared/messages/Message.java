package it.polimi.ingsw.network.shared.messages;

import java.io.Serializable;

/**
 * Default message used when there's no need to notify the client with an object.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Message implements Serializable{
    private final Status status;

    /**
     * Constructor for the class.
     *
     * @param status the message sent.
     *
     * @see Status
     */
    public Message(Status status){
        this.status = status;
    }

    /**
     * Gets the attached message's status.
     *
     * @return the attached message's status.
     *
     * @see Status
     */
    public Status getStatus(){
        return status;
    }
}