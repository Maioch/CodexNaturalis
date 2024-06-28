package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.core.EventHandler;
import it.polimi.ingsw.network.shared.messages.Message;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * RMI-based ExchangeHandler implementation.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see ExchangeHandler
 * @see RMIInterface
 */
public class RMIHandler extends ExchangeHandler implements RMIInterface{

    //the RMI handler that's going to receive the messages sent by this instance.
    private RMIInterface receiverInterface;

    //the executor used to call remote methods without blocking the caller's thread.
    private final ExecutorService executor;

    /**
     * Constructor for the class. It doesn't set the logger (used only by the server).
     *
     * @param handler         the message handler to send the received messages to.
     *
     * @throws RemoteException whenever the remote invocation of this method fails.
     *
     * @see EventHandler
     */
    public RMIHandler(EventHandler<LabeledMessage> handler) throws RemoteException{
        super(handler);
        UnicastRemoteObject.exportObject(this, 0);
        this.receiverInterface = null;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Constructor for the class.
     *
     * @param handler          the message handler to send the received messages to.
     * @param logger           the logger used to log network events.
     *
     * @throws RemoteException whenever the remote invocation of this method fails.
     *
     * @see EventHandler
     */
    public RMIHandler(EventHandler<LabeledMessage> handler, Logger logger) throws RemoteException{
        super(handler, logger);
        UnicastRemoteObject.exportObject(this, 0);
        this.receiverInterface = null;
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Sends messages to a previously set receiver that implements this interface.
     *
     * @param message          the message to send.
     *
     * @throws RemoteException whenever the method invocation fails.
     *
     * @see Message
     */
    @Override
    public void receiveUpdate(Message message) throws RemoteException {
        handler.addEventToQueue(new LabeledMessage(this, message));
    }

    /**
     * Sets the receiver.
     *
     * @param receiverInterface the RMIInterface that will receive the messages.
     *
     * @throws RemoteException  whenever the method invocation fails.
     *
     * @see RMIInterface
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
            executor.shutdown();
            UnicastRemoteObject.unexportObject(this, true);
            if(logger != null) {
                logger.info("RMI handler stopped\n");
            }
        } catch (NoSuchObjectException e) {
            if(logger != null) {
                logger.severe("Could not un-export RMI interface:\n" + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Used by local classes (for example, ClientController on the client's side or ServerMessageHandler
     * on the server's) to send messages through the network.
     *
     * @param message the message to send.
     *
     * @see Message
     */
    @Override
    public void update(Message message){
        if (executor.isShutdown()){
            return;
        }
        //call the method through a single-threaded executor to avoid blocking the controller's thread
        executor.submit(() -> {
            try {
                receiverInterface.receiveUpdate(message);
            } catch (RemoteException e) {
                if(logger != null) {
                    logger.finest("Encountered an IO Exception in RMIHandler:\n" + e.getMessage() + "\n");
                }
            }
        });
    }
}