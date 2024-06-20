package it.polimi.ingsw.network.server;

import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.network.shared.EventHandler;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.RMIHandler;
import it.polimi.ingsw.network.shared.RMIInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * Server-side object that provides clients with their unique remote RMI interface.
 */
public class RMIManager extends UnicastRemoteObject implements RMISetup {
    private final EventHandler<LabeledMessage> messageHandler;
    private final Logger logger;

    /**
     * Constructor for the class.
     * @param messageHandler the message handler that the messages are going to be forwarded to.
     * @throws RemoteException whenever the remote invocation of the method fails.
     */
    public RMIManager(EventHandler<LabeledMessage> messageHandler) throws RemoteException {
        this.messageHandler = messageHandler;
        this.logger = Logger.getLogger(Parameters.getLoggerName());
    }

    /**
     * Remote method that registers an RMI Interface to enable two-way communication between the server and the client.
     * @throws RemoteException whenever the remote invocation of the method fails.
     */
    @Override
    public void register(RMIInterface remoteInterface) throws RemoteException {
        logger.info("New RMI client connected\n");
        RMIHandler rmiHandler = new RMIHandler(messageHandler, logger);
        rmiHandler.setReceiver(remoteInterface);
        remoteInterface.setReceiver(rmiHandler);
    }
}