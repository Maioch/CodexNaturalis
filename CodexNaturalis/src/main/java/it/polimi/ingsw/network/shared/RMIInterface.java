package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Used by the client and the server to send messages to each other through the RMI protocol.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public interface RMIInterface extends Remote {

    /**
     * Sends messages to a previously set receiver that implements this interface.
     *
     * @param message          the message to send.
     *
     * @throws RemoteException whenever the method invocation fails.
     *
     * @see Message
     */
    void receiveUpdate(Message message) throws RemoteException;

    /**
     * Sets the receiver.
     *
     * @param receiverInterface the RMIInterface that will receive the messages.
     *
     * @throws RemoteException  whenever the method invocation fails.
     */
    void setReceiver(RMIInterface receiverInterface) throws RemoteException;
}