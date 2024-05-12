package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GamesManager;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.TCPHandler;
import it.polimi.ingsw.model.server.GameParameters;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
* Class that acts as interface on the internet. It contains the main method for the server.
*
* @author Andrea Fidanza E NESSUN ALTRO
*/
public class Server {

    /**
     * Main method, entry point for the server. It sets up the message handler and starts listening on the two sockets
     * corresponding to the tcp and rmi connections. Each accepted connection is handled by a network handler.
     */
    public static void main(String[] args) throws RemoteException{
        GamesManager games = new GamesManager();
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(games);
        RMIManager rmiManager = new RMIManager(serverMessageHandler);
        LocateRegistry.createRegistry(GameParameters.getRMIPort());
        try {
            Naming.rebind("/RMIManager", rmiManager);
        }catch(MalformedURLException e){
            System.out.println("Couldn't bind RMIManager because the URL it was supposed to bind to is malformed");
            System.out.println(e.getMessage());
        } catch (RemoteException e) {
            System.out.println("Couldn't bind RMIManager because of a remote exception");
            System.out.println(e.getMessage());
        }
        new Thread(serverMessageHandler).start();
        new Thread(()-> {
            try (ServerSocket serverSocket = new ServerSocket(GameParameters.getTCPPort())) {
                System.out.println("TCP server started on port: " + GameParameters.getTCPPort());
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected");
                    try {
                        TCPHandler tcpHandler = new TCPHandler(clientSocket, serverMessageHandler);
                        new Thread(tcpHandler).start();
                    }catch (IOException e){
                        System.out.println("Encountered an IO exception when creating a TCP handler");
                        System.out.println(e.getMessage());
                    }
                }
            } catch (IOException e) {
                System.out.println("Encountered an IO exception when starting the server");
                System.out.println(e.getMessage());
            }
        }).start();
    }
}