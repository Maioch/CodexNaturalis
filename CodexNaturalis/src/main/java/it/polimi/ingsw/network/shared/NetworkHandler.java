package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.controller.server.GameController;

import java.util.logging.Logger;

/**
 * Class that represents the objects that handle each client connected to the server.
 */
public abstract class NetworkHandler implements Listener {

    protected final EventHandler<LabeledMessage> handler;
    protected final Logger logger;
    private GameController currentGame;
    private boolean isDisconnected;

    /**
     * Constructor for the class.
     * @param handler the message handler associated to the client.
     */
    public NetworkHandler(EventHandler<LabeledMessage> handler) {
        this.currentGame = null;
        this.handler = handler;
        this.logger = null;
        this.isDisconnected = false;
    }

    /**
     * Constructor for the class.
     * @param handler the message handler associated to the client.
     * @param logger  the logger used to log network events
     */
    public NetworkHandler(EventHandler<LabeledMessage> handler, Logger logger) {
        this.currentGame = null;
        this.handler = handler;
        this.logger = logger;
        this.isDisconnected = false;
    }

    /**
     * @return the game that the player associated to this handler is in.
     */
    public GameController getCurrentGame(){
        return currentGame;
    }

    /**
     * Setter for the currentGame attribute.
     * @param currentGame the game associated to the handler.
     */
    public void setCurrentGame(GameController currentGame){
        this.currentGame = currentGame;
    }

    /**
     * Sets the disconnected flag to true
     */
    public synchronized void setDisconnected(){
        this.isDisconnected = true;
    }

    /**
     * Method to check whether the client is disconnected
     * @return true if the client is disconnected
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public synchronized boolean isDisconnected(){
        return isDisconnected;
    }

    /**
     * Stops the currently running network handler instance.
     */
    public abstract void stop();
}