package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.server.model.GameParameters;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static ConcurrentHashMap<Integer, GameController> matches;

    public static void main(String[] args){
        matches = new ConcurrentHashMap<>();
        ClientMessageHandler clientMessageHandler = new ClientMessageHandler();
        new Thread(clientMessageHandler).start();
        new Thread(()-> {
            try (ServerSocket serverSocket = new ServerSocket(GameParameters.getPort())) {
                System.out.println("TCP server started on port: " + GameParameters.getPort());
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected");
                    try {
                        ClientHandler clientHandler = new TCPClientHandler(clientSocket, clientMessageHandler);
                        new Thread(clientHandler).start();
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

    public static HashMap<Integer, String> getFormattedMatches(){
        return new HashMap<>(){{
            for(Entry<Integer, GameController> entry : matches.entrySet()){
                put(entry.getKey(), entry.getValue().getName());
            }
        }};
    }

    public static GameController getMatch(int id){
        return matches.get(id);
    }

    public static int addMatch(int numberOfPlayers, String name) throws IllegalNumberOfPlayers{
        int id = (!matches.containsKey(1)) ? 0 :
                matches.keySet().stream()
                .filter(x -> matches.get(x + 1) == null)
                .min(Integer::compareTo).orElse(0);
        matches.put(id + 1, new GameController(numberOfPlayers, new ServerSubject(), name));
        return id + 1;
    }
}