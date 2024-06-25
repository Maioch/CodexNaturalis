package it.polimi.ingsw.network.server;

import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.core.EventHandler;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.RMIHandler;
import it.polimi.ingsw.network.shared.RMIInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.logging.Logger;

/**
 * Server-side object that provides clients with their unique remote RMI interface.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see RMISetup
 * @see RMIInterface
 */
public class RMIManager extends UnicastRemoteObject implements RMISetup {

    //the handler that is going to handle the messages sent to the RMI handlers created by this class
    private final EventHandler<LabeledMessage> messageHandler;

    //keeps track of and manages all the created handlers
    private final ExchangeHandlerManager exchangeHandlerManager;

    //the logger that will log info about the RMI handlers
    private final Logger logger;

    /**
     * Constructor for the class.
     *
     * @param messageHandler   the event handler that the messages are going to be forwarded to.
     *
     * @throws RemoteException whenever the remote invocation of the method fails.
     *
     * @see EventHandler
     * @see LabeledMessage
     */
    public RMIManager(EventHandler<LabeledMessage> messageHandler, ExchangeHandlerManager exchangeHandlerManager) throws RemoteException {
        this.messageHandler = messageHandler;
        this.exchangeHandlerManager = exchangeHandlerManager;
        this.logger = Logger.getLogger(Parameters.getLoggerName());
    }

    /**
     * Remote method that registers an RMI Interface to enable two-way communication between the server and the client.
     *
     * @param remoteInterface  the remote interface used to communicate.
     *
     * @throws RemoteException whenever the remote invocation of the method fails.
     *
     * @see RMIInterface
     */
    @Override
    public void register(RMIInterface remoteInterface) throws RemoteException {
        logger.info("New RMI client connected\n");
        RMIHandler rmiHandler = new RMIHandler(messageHandler, logger);
        rmiHandler.setReceiver(remoteInterface);
        remoteInterface.setReceiver(rmiHandler);
        exchangeHandlerManager.addHandler(rmiHandler);
    }
}