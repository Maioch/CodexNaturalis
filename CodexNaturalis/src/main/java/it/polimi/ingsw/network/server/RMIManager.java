package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.RMIInterface;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

/**
 * server-side object that provides clients with their unique remote RMI interface.
 */
public class RMIManager extends UnicastRemoteObject implements RMIHandlerProvider{
    private final MessageHandler messageHandler;

    /**
     * constructor for RMIManager
     * @param messageHandler the message handler that the messages are going to be forwarded to
     * @throws RemoteException whenever the remote invocation of the method fails
     */
    public RMIManager(MessageHandler messageHandler) throws RemoteException{
        this.messageHandler = messageHandler;
    }

    /**
     * Remote method that returns a unique RMIInterface the client can use to contact the server
     * @return a unique RMIInterface
     * @throws RemoteException whenever the remote invocation of the method fails
     */
    @Override
    public RMIInterface getRemoteHandler() throws RemoteException {
        return new RMIHandler(messageHandler);
    }
}