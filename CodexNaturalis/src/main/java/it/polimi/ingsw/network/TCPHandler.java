package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * TCP-based NetworkHandler implementation.
 */

public class TCPHandler extends NetworkHandler implements Runnable{
    private final ObjectOutputStream socketOutput;
    private final ObjectInputStream socketInput;

    /**
     * Constructor for the class.
     * @param socket the socket to which the client is connected.
     * @param handler the message handler that will handle the messages received.
     */
    public TCPHandler(Socket socket, EventHandler<LabeledMessage> handler) throws IOException{
        super(handler);
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Main method run by the thread.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        try {
            while (true) {
                try {
                    Message message = (Message) socketInput.readObject();
                    if(message.getStatus() == Status.REQUEST_PING){
                        update(new Message(Status.PING_ACK));
                        continue;
                    }
                    handler.addEventToQueue(new LabeledMessage(this, message));
                } catch (ClassNotFoundException e) {
                    System.out.println("Received an invalid message");
                }
            }
        }catch (IOException e){
            System.out.println("Encountered an IO Exception in TCPHandler run");
            System.out.println(e.getMessage());
        }
    }

    /**
     * Method used to write a message on the output stream.
     * @param message the message to write.
     */
    @Override
    public void update(Message message){
        try{
            socketOutput.writeObject(message);
        }catch(IOException e){
            //needs a better way to be handled
            System.out.println("Encountered an IO Exception in TCPHandler update");
            System.out.println(e.getMessage());
        }
    }
}