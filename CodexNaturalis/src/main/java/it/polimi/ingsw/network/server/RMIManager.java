package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.RMIHandler;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Handler;

public class RMIManager extends UnicastRemoteObject implements RMIHandlerProvider{
    private final MessageHandler messageHandler;

    public RMIManager(MessageHandler messageHandler) throws RemoteException{
        this.messageHandler = messageHandler;
    }

    public RMIHandler register() throws IOException {
        return new RMIHandler(messageHandler);
    }
}