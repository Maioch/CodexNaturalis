package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

import java.rmi.RemoteException;

/**
 * RMI-based NetworkHandler implementation.
 */
public class RMIHandler extends NetworkHandler implements RMIInterface{
    private RMIInterface receiverInterface;

    /**
     * Constructor for the class.
     * @param handler the message handler to send the received messages to.
     * @throws RemoteException whenever the remote invocation of this method fails.
     */
    public RMIHandler(MessageHandler handler) throws RemoteException{
        super(handler);
        receiverInterface = null;
    }

    /**
     * Remote method used to send messages to a previously set receiver that implements this interface.
     * @param message the message to send.
     * @throws RemoteException whenever the method invocation fails.
     */
    @Override
    public void receiveUpdate(Message message) throws RemoteException {
        //System.out.println(message.getStatus());
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
        try {
            //System.out.println(message.getStatus());
            receiverInterface.receiveUpdate(message);
        }catch (RemoteException e){
            System.out.println(e.getMessage());
        }
    }
}