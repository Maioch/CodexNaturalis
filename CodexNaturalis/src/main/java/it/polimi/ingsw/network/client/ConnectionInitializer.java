package it.polimi.ingsw.network.client;

import it.polimi.ingsw.controller.client.ClientController;
import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.network.shared.RMIHandler;
import it.polimi.ingsw.network.shared.TCPHandler;
import it.polimi.ingsw.network.server.RMISetup;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Class used to initialize the client's connection to the server, both using TCP and RMI.
 */
public class ConnectionInitializer {

    /**
     * Method that initializes the TCP connection.
     *
     * @param settings               the server's ip, port and connection type (RMI or TCP).
     * @param controller             the controller that will handle messages and client model updates.
     * @throws MalformedURLException when the server ip isn't correct.
     * @throws NotBoundException     when the requested object isn't bound.
     * @throws RemoteException       when an RMI connection error occurs.
     * @throws TCPException          when a TCP connection error occurs.
     */
    public static void initializeConnection(ConnectionSettings settings, ClientController controller)
            throws TCPException, RemoteException, MalformedURLException, NotBoundException {
        if(settings.type() == ConnectionSettings.ConnectionType.TCP) {
            try {
                TCPHandler tcpHandler = new TCPHandler(new Socket(settings.ip(), settings.port()), controller);
                new Thread(tcpHandler).start();
                controller.setNetworkHandler(tcpHandler);
            } catch (IOException e){
                throw new TCPException();
            }
        }else{
            RMISetup rmiSetup = (RMISetup) Naming.lookup(String.format("//%s:%d/RMIManager", settings.ip(), settings.port()));
            RMIHandler rmiHandler = new RMIHandler(controller);
            rmiSetup.register(rmiHandler);
            controller.setNetworkHandler(rmiHandler);
        }
    }
}