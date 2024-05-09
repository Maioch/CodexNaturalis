package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.RMIInterface;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface for a server-side object that provides clients with their unique remote RMI interface.
 */
public interface RMIHandlerProvider extends Remote {
    /**
     * Remote method that returns a unique RMIInterface the client can use to contact the server
     * @return a unique RMIInterface
     * @throws RemoteException whenever the method is unable to create a new RMIInterface
     */
    RMIInterface getRemoteHandler() throws RemoteException;
}
