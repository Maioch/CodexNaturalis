package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface used by the client and the server to send messages to each other through the RMI protocol.
 */
public interface RMIInterface extends Remote {

    /**
     * Remote method used to send messages to a previously set receiver that implements this interface.
     * @param message the message to send.
     * @throws RemoteException whenever the method invocation fails.
     */
    void receiveUpdate(Message message) throws RemoteException;

    /**
     * Setter for the receiver.
     * @param receiverInterface the RMIInterface that will receive the messages.
     * @throws RemoteException whenever the method invocation fails.
     */
    void setReceiver(RMIInterface receiverInterface) throws RemoteException;
}