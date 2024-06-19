package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.shared.EventHandler;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.RMIHandler;
import it.polimi.ingsw.network.shared.RMIInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Server-side object that provides clients with their unique remote RMI interface.
 */
public class RMIManager extends UnicastRemoteObject implements RMISetup {
    private final EventHandler<LabeledMessage> messageHandler;

    /**
     * Constructor for the class.
     * @param messageHandler the message handler that the messages are going to be forwarded to.
     * @throws RemoteException whenever the remote invocation of the method fails.
     */
    public RMIManager(EventHandler<LabeledMessage> messageHandler) throws RemoteException{
        this.messageHandler = messageHandler;
    }

    /**
     * Remote method that registers an RMI Interface to enable two-way communication between the server and the client.
     * @throws RemoteException whenever the remote invocation of the method fails.
     */
    @Override
    public void register(RMIInterface remoteInterface) throws RemoteException {
        System.out.println("New RMI client connected");
        RMIHandler rmiHandler = new RMIHandler(messageHandler);
        rmiHandler.setReceiver(remoteInterface);
        remoteInterface.setReceiver(rmiHandler);
    }
}