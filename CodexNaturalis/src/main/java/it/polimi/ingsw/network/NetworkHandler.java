package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class that represents the objects that handle each client connected to the server.
 */
public abstract class NetworkHandler implements Listener {
    protected final EventHandler<LabeledMessage> handler;
    private GameController currentGame;
    private boolean isDisconnected;

    /**
     * Constructor for the class.
     * @param handler the message handler associated to the client.
     */
    public NetworkHandler(EventHandler<LabeledMessage> handler) throws RemoteException {
        this.currentGame = null;
        this.handler = handler;
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
    public synchronized boolean isDisconnected(){
        return isDisconnected;
    }
}