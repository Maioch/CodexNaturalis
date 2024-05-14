package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * TCP-based NetworkHandler implementation
 */

public class TCPHandler extends NetworkHandler implements Runnable{
    private final ObjectOutputStream socketOutput;
    private final ObjectInputStream socketInput;

    /**
     * Constructor for the class
     * @param socket the socket to which the client is connected
     * @param handler the message handler that will handle the messages received
     */
    public TCPHandler(Socket socket, MessageHandler handler) throws IOException{
        super(handler);
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Main method run by the thread
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        try {
            while (true) {
                try {
                    Message message = (Message) socketInput.readObject();
                    //System.out.println(message.getStatus());
                    handler.addMessageToQueue(message, this);
                    //System.out.printf("Received %s%n",message.getStatus().toString());
                } catch (ClassNotFoundException e) {
                    System.out.println("Received an invalid message");
                }
            }
        }catch (IOException e){
            System.out.println("Encountered an IO Exception in TCPHandler");
            e.printStackTrace();
            //System.out.println(e.getMessage());
        }
    }

    /**
     * Implemented update method. It writes the message on the output stream
     * @param message the message to write
     */
    @Override
    public void update(Message message){
        //System.out.printf("Sent %s%n",message.getStatus().toString());
        try{
            socketOutput.writeObject(message);
        }catch(IOException e){
            //needs a better way to be handled
            System.out.println(e.getMessage());
        }
    }
}