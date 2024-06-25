package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

/**
 * Interface used to label every server listener.
 */
public interface Listener {

    /**
     * Method used to write a message on the output stream.
     *
     * @param message the message to write.
     */
    void update(Message message);
}