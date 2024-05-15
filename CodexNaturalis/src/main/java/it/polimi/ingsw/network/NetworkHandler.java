package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Class that represents the objects that handle each client connected to the server.
 */
public abstract class NetworkHandler extends UnicastRemoteObject implements Listener {
    protected final MessageHandler handler;
    private GameController currentGame;

    /**
     * Constructor for the class.
     * @param handler the message handler associated to the client.
     */
    public NetworkHandler(MessageHandler handler) throws RemoteException {
        this.currentGame = null;
        this.handler = handler;
    }

    /**
     * @return the game that the player associated to this handler is in.
     */
    public GameController getCurrentGame(){
        return currentGame;
    }

    /**
     * Setter fot eh currentGame attribute.
     * @param currentGame the game associated to the hanlder.
     */
    public void setCurrentGame(GameController currentGame){
        this.currentGame = currentGame;
    }
}