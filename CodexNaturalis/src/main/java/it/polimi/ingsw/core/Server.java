package it.polimi.ingsw.core;

import it.polimi.ingsw.controller.server.GamesManager;
import it.polimi.ingsw.network.server.RMIManager;
import it.polimi.ingsw.controller.server.ServerMessageHandler;
import it.polimi.ingsw.network.shared.TCPHandler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
* Class that acts as interface on the internet. It contains the main method for the server.
*
* @author Andrea Fidanza
*/
public class Server {

    /**
     * Main method, entry point for the server. It sets up the message handler and starts listening on the two sockets
     * corresponding to the tcp and rmi connections. Each accepted connection is handled by a network handler.
     */
    public static void main(String[] args) throws RemoteException{
        Logger logger = createLogger();
        GamesManager games = new GamesManager();
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(games);
        RMIManager rmiManager = new RMIManager(serverMessageHandler);
        LocateRegistry.createRegistry(Parameters.getRMIPort());
        try {
            Naming.rebind("/RMIManager", rmiManager);
            logger.info("RMI server started on port: " + Parameters.getRMIPort() + "\n");
        } catch(MalformedURLException e) {
            logger.severe("Couldn't bind RMIManager because the URL it was supposed to bind to is malformed:\n"
                    + e.getMessage() + "\n");
        } catch (RemoteException e) {
            logger.warning("Couldn't bind RMIManager because of a remote exception:\n" + e.getMessage() + "\n");
        }
        new Thread(()-> {
            try (ServerSocket serverSocket = new ServerSocket(Parameters.getTCPPort())) {
                logger.info("TCP server started on port: " + Parameters.getTCPPort() + "\n");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    logger.info("New TCP client connected\n");
                    try {
                        TCPHandler tcpHandler = new TCPHandler(clientSocket, serverMessageHandler, logger);
                        new Thread(tcpHandler).start();
                    } catch (IOException e) {
                        logger.warning("Encountered an IO exception when creating the TCP handler:\n" + e.getMessage() + "\n");
                    }
                }
            } catch (IOException e) {
                logger.severe("Encountered an IO exception when starting the server:\n" + e.getMessage() + "\n");
            }
        }).start();
        new Thread(serverMessageHandler).start();
    }

    /**
     * Creates a logger file.
     * @return the created logger.
     */
    private static Logger createLogger() {
        Logger logger = Logger.getLogger("Server");
        try {
            FileHandler file = new FileHandler("Server.log");
            file.setFormatter(new SimpleFormatter());
            logger.addHandler(file);
        } catch (IOException e) {
            logger.warning("Couldn't create a log file\n");
        }
        return logger;
    }
}