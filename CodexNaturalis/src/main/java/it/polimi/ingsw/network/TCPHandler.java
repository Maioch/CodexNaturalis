package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.server.ServerMessageHandler;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPHandler extends NetworkHandler {
    private final ObjectOutputStream socketOutput;
    private final ObjectInputStream socketInput;

    /**
     * Constructor for the class
     * @param socket the socket to which the client is connected
     * @param handler the message handler that will handle the messages received
     */
    public TCPHandler(Socket socket, ServerMessageHandler handler) throws IOException{
        super(handler);
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Main method run by the thread
     */
    @Override
    public void run(){
        try{
            while(true){
                try{
                    Message message = (Message) socketInput.readObject();
                    handler.addMessageToQueue(message,this);
                }catch (ClassNotFoundException e){
                    System.out.println("Received an invalid message");
                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Implemented update method. It writes the message on the output stream
     * @param message the message to write
     */
    @Override
    public void update(Message message){
        try{
            socketOutput.writeObject(message);
        }catch(IOException e){
            //needs a better way to be handled
            System.out.println(e.getMessage());
        }
    }
}