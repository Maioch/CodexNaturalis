package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;

import java.io.*;

/**
 * Class used to handle each client connected to the server
 */
public abstract class ClientHandler implements Runnable, ServerListener{
    protected final ClientMessageHandler handler;
    private GameController currentGame;

    /**
     * Constructor for the class
     * @param handler the message handler to which the client refers to
     */
    public ClientHandler(ClientMessageHandler handler) throws IOException{
        this.currentGame = null;
        this.handler = handler;
    }

    public GameController getCurrentGame() {
        return currentGame;
    }

    public void setCurrentGame(GameController currentGame){
        this.currentGame = currentGame;
    }
}