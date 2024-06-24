package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.shared.RMIInterface;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Server-side object that registers clients with their unique remote RMI interface.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see RMIInterface
 */
public interface RMISetup extends Remote {

    /**
     * Remote method that registers an RMI Interface to enable two-way communication between the server and the client.
     *
     * @param remoteInterface  the remote used to communicate.
     *
     * @throws RemoteException whenever the method is unable to create a new RMIInterface.
     *
     * @see RMIInterface
     */
    void register(RMIInterface remoteInterface) throws RemoteException;
}