package it.polimi.ingsw.network.server;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.messages.*;

import java.io.*;
import java.net.Socket;

/**
 * Class used to handle each client connected to the server
 */
public class ClientHandler implements Runnable{
    private final Socket socket;

    /**
     * Constructor for the class
     * @param socket the socket to which the client is connected
     */
    public ClientHandler(Socket socket){
        this.socket = socket;
    }

    /**
     * Main method run by the thread
     */
    @Override
    public void run(){
        try {
            ObjectOutputStream clientOutput = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream clientInput = new ObjectInputStream(socket.getInputStream());
            while (socket.isConnected()) {
                try {
                    Message message = (Message) clientInput.readObject();
                    switch (message.getStatus()){
                        case SHOW_MATCHES -> clientOutput.writeObject(new MatchListMessage(Server.getMatches()));
                        case NEW_GAME -> {
                            if (message instanceof NewGameMessage){
                                NewGameMessage newGameMessage = (NewGameMessage) message;
                                try{
                                    int gameId = Server.addMatch(newGameMessage.getNumberOfPlayers(), newGameMessage.getName());
                                    clientOutput.writeObject(new IntegerMessage(Status.NEW_GAME,gameId));
                                }catch (IllegalNumberOfPlayers e){
                                    clientOutput.writeObject(new Message(Status.NEW_GAME_FAIL));
                                }
                            }
                        }
                        default ->
                    }
                }catch (ClassNotFoundException e){
                    System.out.println("Received an invalid message");
                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }
}
