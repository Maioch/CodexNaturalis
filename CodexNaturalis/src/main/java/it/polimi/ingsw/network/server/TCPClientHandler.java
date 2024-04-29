package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPClientHandler extends ClientHandler{
    private final Socket socket;
    private final ObjectOutputStream clientOutput;
    private final ObjectInputStream clientInput;

    /**
     * Constructor for the class
     * @param socket the socket to which the client is connected
     */
    public TCPClientHandler(Socket socket, ClientMessageHandler handler) throws IOException {
        super(handler);
        this.socket = socket;
        this.clientOutput = new ObjectOutputStream(socket.getOutputStream());
        this.clientInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Main method run by the thread
     */
    @Override
    public void run(){
        try {
            while (socket.isConnected()) {
                try {
                    Message message = (Message) clientInput.readObject();
                    handler.addMessageToQueue(message,this);
                }catch (ClassNotFoundException e){
                    System.out.println("Received an invalid message");
                }
            }
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void update(Message message) {
        try {
            clientOutput.writeObject(message);
        }catch (IOException e){
            //needs a better way to be handled
            System.out.println(e.getMessage());
        }
    }
}