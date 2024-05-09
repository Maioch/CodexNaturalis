package it.polimi.ingsw.network;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.server.ServerMessageHandler;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Protocol independent class used to handle each client connected to the server
 */
public abstract class NetworkHandler extends UnicastRemoteObject implements Listener {
    protected final MessageHandler handler;
    private GameController currentGame;

    /**
     * Constructor for the class
     *
     * @param handler the message handler to which the client refers to
     */
    public NetworkHandler(MessageHandler handler) throws RemoteException {
        this.currentGame = null;
        this.handler = handler;
    }

    /**
     * @return the game that the player associated to this handler is playing
     */
    public GameController getCurrentGame(){
        return currentGame;
    }

    /**
     * @param currentGame the game to set
     */
    public void setCurrentGame(GameController currentGame){
        this.currentGame = currentGame;
    }
}