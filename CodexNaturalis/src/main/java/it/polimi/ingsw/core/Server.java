package it.polimi.ingsw.core;

import it.polimi.ingsw.controller.server.GamesManager;
import it.polimi.ingsw.network.server.ExchangeHandlerManager;
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
* Acts as interface on the internet. It contains the main method for the server.
*
* @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
*/
public class Server {

    /**
     * Main method, entry point for the server. It sets up the logger, the message handler, the games manager
     * and the rmi manager. Finally, starts listening on the tcp socket.
     * Each accepted connection is handled by a exchange handler.
     *
     * @param args not used
     */
    public static void main(String[] args){
        Logger logger = createLogger();
        ExchangeHandlerManager exchangeHandlerManager = new ExchangeHandlerManager();
        GamesManager games = new GamesManager();
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(games, exchangeHandlerManager);
        try {
            RMIManager rmiManager = new RMIManager(serverMessageHandler, exchangeHandlerManager);
            LocateRegistry.createRegistry(Parameters.getRMIPort());
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
                        exchangeHandlerManager.addHandler(tcpHandler);
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
     *
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