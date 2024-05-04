package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GamesManager;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.TCPHandler;
import it.polimi.ingsw.model.server.GameParameters;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
* Class that acts as interface on the internet. It contains the main method for the server
*
* @author Andrea Fidanza E NESSUN ALTRO
*/
public class Server {

    /**
     * Main method, entry point for the server. It sets up the message handler
     * and starts listening on the two sockets corresponding to the tcp and rmi connections.
     * Each accepted connection is handled by a network handler
     */
    public static void main(String[] args){
        GamesManager games = new GamesManager();
        ServerMessageHandler serverMessageHandler = new ServerMessageHandler(games);
        new Thread(serverMessageHandler).start();
        new Thread(()-> {
            try (ServerSocket serverSocket = new ServerSocket(GameParameters.getTCPPort())) {
                System.out.println("TCP server started on port: " + GameParameters.getTCPPort());
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected");
                    try {
                        NetworkHandler networkHandler = new TCPHandler(clientSocket, serverMessageHandler);
                        new Thread(networkHandler).start();
                    }catch (IOException e){
                        System.out.println("Encountered an IO exception when creating a clientHandler");
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