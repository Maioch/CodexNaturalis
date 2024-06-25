package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.controller.server.GameController;
import it.polimi.ingsw.core.EventHandler;

import java.util.logging.Logger;

/**
 * Represents the objects that handle each client connected to the server.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public abstract class NetworkHandler implements Listener {

    //the handler that's supposed to handle the received messages.
    protected final EventHandler<LabeledMessage> handler;

    //the logger that, when present, will log info about this handler's state.
    protected final Logger logger;

    //the handler's current game, if it's part of any.
    private GameController currentGame;

    //the game attribute's lock
    private final Object gameLock = new Object();

    //whether the handler is considered disconnected
    private boolean isDisconnected;

    /**
     * Constructor for the class.
     *
     * @param handler the message handler associated to the client.
     *
     * @see EventHandler
     */
    public NetworkHandler(EventHandler<LabeledMessage> handler) {
        this.currentGame = null;
        this.handler = handler;
        this.logger = null;
        this.isDisconnected = false;
    }

    /**
     * Constructor for the class.
     *
     * @param handler the message handler associated to the client.
     * @param logger  the logger used to log network events.
     *
     * @see EventHandler
     */
    public NetworkHandler(EventHandler<LabeledMessage> handler, Logger logger) {
        this.currentGame = null;
        this.handler = handler;
        this.logger = logger;
        this.isDisconnected = false;
    }

    /**
     * Gets the game controller associated to this handler.
     *
     * @return the game that the player associated to this handler is in.
     *
     * @see GameController
     */
    public GameController getCurrentGame(){
        synchronized (gameLock) {
            return currentGame;
        }
    }

    /**
     * Setter for the currentGame attribute.
     *
     * @param currentGame the game associated to the handler.
     *
     * @see GameController
     */
    public void setCurrentGame(GameController currentGame){
        synchronized (gameLock) {
            this.currentGame = currentGame;
        }
    }

    /**
     * Sets the disconnected flag to true.
     */
    public synchronized void setDisconnected(){
        this.isDisconnected = true;
    }

    /**
     * Checks whether the client is disconnected.
     *
     * @return true if the client is disconnected.
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