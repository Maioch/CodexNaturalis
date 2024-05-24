package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public RMIHandler(MessageHandler handler) throws RemoteException{
        super(handler);
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
        //System.out.println(message.getStatus());
        if(message.getStatus() == Status.REQUEST_PING){
            receiverInterface.receiveUpdate(new Message(Status.PING_ACK));
            return;
        }
        handler.addMessageToQueue(message,this);
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
     * Method used by local classes (for example, ClientController on the client's side or ServerMessageHandler
     * on the server's) to send messages through the network.
     * @param message the message to send.
     */
    @Override
    public void update(Message message){
        //System.out.println(message.getStatus());
        //call the method through a single-threaded executor to avoid blocking the controller's thread
        executor.submit(() -> {
            try {
                receiverInterface.receiveUpdate(message);
            } catch (RemoteException e) {
                System.out.println("Encountered an IO Exception in RMIHandler update");
                System.out.println(e.getMessage());
            }
        });
    }
}