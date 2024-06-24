package it.polimi.ingsw.view;

/**
 * Represents view classes that implement reconnection.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public interface ReconnectableView {

    /**
     * Show a message of disconnection.
     * This occurs if the client has not received a ping ack (from the server) for a parametrically-specified amount of time.
     */
    void showDisconnectionMessage();
}