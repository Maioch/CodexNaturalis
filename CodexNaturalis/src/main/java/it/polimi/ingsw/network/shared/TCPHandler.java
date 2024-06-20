package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * TCP-based NetworkHandler implementation.
 */

public class TCPHandler extends NetworkHandler implements Runnable{

    private final ObjectOutputStream socketOutput;
    private final ObjectInputStream socketInput;
    private Thread handlerThread;

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
     * Constructor for the class.
     * @param socket the socket to which the client is connected.
     * @param handler the message handler that will handle the messages received.
     */
    public TCPHandler(Socket socket, EventHandler<LabeledMessage> handler, Logger logger) throws IOException{
        super(handler, logger);
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Stops this handler running thread.
     */
    @Override
    public void stop(){
        handlerThread.interrupt();
    }

    /**
     * Main method run by the thread.
     */
    @Override
    public void run(){
        handlerThread = Thread.currentThread();
        try {
            while (!handlerThread.isInterrupted()) {
                try {
                    Message message = (Message) socketInput.readObject();
                    handler.addEventToQueue(new LabeledMessage(this, message));
                } catch (ClassNotFoundException e) {
                    if(logger != null) {
                        logger.warning("Received an invalid message:\n" + e.getMessage() + "\n");
                    }
                }
            }
        }catch (IOException e){
            if(logger != null) {
                logger.info("Encountered an IO Exception in TCPHandler:\n" + e.getMessage() + "\n");
            }
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
            if(logger != null) {
                logger.info("Encountered an IO Exception in TCPHandler:\n" + e.getMessage() + "\n");
            }
        }
    }
}