package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIInterface extends Remote {
    void receiveUpdate(Message message) throws RemoteException;
    void setCallback(RMIInterface clientInterface) throws RemoteException;
}
