package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.shared.RMIInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a server-side object that provides clients with their unique remote RMI interface.
 */
public interface RMISetup extends Remote {
    /**
     * Remote method that registers an RMI Interface to enable two-way communication between the server and the client.
     * @throws RemoteException whenever the method is unable to create a new RMIInterface.
     */
    void register(RMIInterface remoteInterface) throws RemoteException;
}