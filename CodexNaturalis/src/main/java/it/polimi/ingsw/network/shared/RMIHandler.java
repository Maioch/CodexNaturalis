package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * RMI-based NetworkHandler implementation.
 */
public class RMIHandler extends NetworkHandler implements RMIInterface{
    private RMIInterface receiverInterface;
    private final ExecutorService executor;

    /**
     * Constructor for the class.
     * @param handler the message handler to send the received messages to.
     * @throws RemoteException whenever the remote invocation of this method fails.
     */
    public RMIHandler(EventHandler<LabeledMessage> handler) throws RemoteException{
        super(handler);
        UnicastRemoteObject.exportObject(this, 0);
        this.receiverInterface = null;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Constructor for the class.
     * @param handler the message handler to send the received messages to.
     * @param logger  the logger used to log network events
     * @throws RemoteException whenever the remote invocation of this method fails.
     */
    public RMIHandler(EventHandler<LabeledMessage> handler, Logger logger) throws RemoteException{
        super(handler, logger);
        UnicastRemoteObject.exportObject(this, 0);
        this.receiverInterface = null;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Remote method used to send messages to a previously set receiver that implements this interface.
     * @param message the message to send.
     * @throws RemoteException whenever the method invocation fails.
     */
    @Override
    public void receiveUpdate(Message message) throws RemoteException {
        handler.addEventToQueue(new LabeledMessage(this, message));
    }

    /**
     * Setter for the receiver.
     * @param receiverInterface the RMIInterface that will receive the messages.
     * @throws RemoteException whenever the method invocation fails.
     */
    @Override
    public void setReceiver(RMIInterface receiverInterface) throws RemoteException{
        this.receiverInterface = receiverInterface;
    }

    /**
     * Stops this handler.
     */
    @Override
    public void stop(){
        try {
            UnicastRemoteObject.unexportObject(this, true);
        } catch (NoSuchObjectException e) {
            if(logger != null) {
                logger.severe("Could not un-export RMI interface:\n" + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Method used by local classes (for example, ClientController on the client's side or ServerMessageHandler
     * on the server's) to send messages through the network.
     * @param message the message to send.
     */
    @Override
    public void update(Message message){
        //call the method through a single-threaded executor to avoid blocking the controller's thread
        executor.submit(() -> {
            try {
                receiverInterface.receiveUpdate(message);
            } catch (RemoteException e) {
                if(logger != null) {
                    logger.info("Encountered an IO Exception in RMIHandler:\n" + e.getMessage() + "\n");
                }
            }
        });
    }
}