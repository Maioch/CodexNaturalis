package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.TCPHandler;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.server.RMISetup;
import it.polimi.ingsw.view.EventSubmitter;

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
     * @param ip the server's ip.
     * @param port the server's port.
     * @param controller the controller that will handle the client.
     * @throws IOException when a connection error occurs.
     */
    public static void initializeTCP(String ip, int port, ClientController controller, EventSubmitter eventSubmitter) throws IOException {
        Socket socket = new Socket(ip, port);
        socket.getInputStream();
        TCPHandler tcpHandler = new TCPHandler(socket, controller);
        eventSubmitter.submit(() -> completeTCPSetup(socket, tcpHandler, controller));
    }


    private static void completeTCPSetup(Socket socket, TCPHandler tcpHandler,ClientController controller){
        controller.setNetworkHandler(tcpHandler);
        new Thread(tcpHandler).start();
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    private static void completeRMISetup(ClientController controller, RMIHandler rmiHandler){
        controller.setNetworkHandler(rmiHandler);
        System.out.println(GameParameters.getTitle());
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Method that initializes the RMI connection.
     * @param ip the server's ip.
     * @param port the server's port.
     * @param controller the controller that will handle the client.
     * @throws RemoteException when a connection error occurs.
     * @throws MalformedURLException when the server ip isn't correct.
     * @throws NotBoundException when the requested object isn't bound.
     */
    public static void initializeRMI(String ip, int port, ClientController controller,EventSubmitter eventSubmitter) throws RemoteException, MalformedURLException, NotBoundException {
        RMISetup rmiSetup = (RMISetup) Naming.lookup(String.format("//%s:%d/RMIManager", ip, port));
        RMIHandler rmiHandler = new RMIHandler(controller);
        rmiSetup.register(rmiHandler);
        eventSubmitter.submit(() -> completeRMISetup(controller, rmiHandler));
    }
}