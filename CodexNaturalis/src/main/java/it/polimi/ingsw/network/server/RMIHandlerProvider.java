package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.RMIHandler;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIHandlerProvider extends Remote {
    RMIHandler register() throws RemoteException, IOException;
}
