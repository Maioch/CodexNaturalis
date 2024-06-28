package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.core.EventHandler;
import it.polimi.ingsw.network.shared.messages.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * TCP-based ExchangeHandler implementation.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */

public class TCPHandler extends ExchangeHandler implements Runnable{

    //the TCP socket's output.
    private final ObjectOutputStream socketOutput;

    //the TCP socket's input.
    private final ObjectInputStream socketInput;

    //the TCP socket
    private final Socket socket;

    //the thread that listens for incoming messages.
    private Thread handlerThread;

    /**
     * Constructor for the class. It doesn't set the logger (used only by the server).
     *
     * @param socket       the socket to which the client is connected.
     * @param handler      the message handler that will handle the messages received.
     *
     * @see EventHandler
     *
     * @throws IOException if an I/O error occurs.
     */
    public TCPHandler(Socket socket, EventHandler<LabeledMessage> handler) throws IOException{
        super(handler);
        this.socket = socket;
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Constructor for the class.
     *
     * @param socket       the socket to which the client is connected.
     * @param handler      the message handler that will handle the messages received.
     * @param logger       the logger used to log network events.
     *
     * @see EventHandler
     *
     * @throws IOException if an I/O error occurs.
     */
    public TCPHandler(Socket socket, EventHandler<LabeledMessage> handler, Logger logger) throws IOException{
        super(handler, logger);
        this.socket = socket;
        this.socketOutput = new ObjectOutputStream(socket.getOutputStream());
        this.socketInput = new ObjectInputStream(socket.getInputStream());
    }

    /**
     * Stops this handler's running thread.
     */
    @Override
    public void stop(){
        handlerThread.interrupt();
        if(logger != null) {
            logger.info("TCP handler stopped\n");
        }
    }

    /**
     * Main method run by the thread. Reads from the input stream and add the message read to the event handler's queue.
     */
    @Override
    public void run(){
        handlerThread = Thread.currentThread();
        while (!handlerThread.isInterrupted()) {
            try {
                Message message = (Message) socketInput.readObject();
                handler.addEventToQueue(new LabeledMessage(this, message));
            } catch (ClassNotFoundException e) {
                if(logger != null) {
                    logger.warning("Received an invalid message:\n" + e.getMessage() + "\n");
                }
            } catch (IOException e){
                if(logger != null) {
                    logger.finest("Encountered an IO Exception in TCPHandler:\n" + e.getMessage() + "\n");
                }
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            if(logger != null) {
                logger.info("Couldn't close the socket:\n" + e.getMessage() + "\n");
            }
        }
    }

    /**
     * Writes a message on the output stream.
     *
     * @param message the message to write.
     *
     * @see Message
     */
    @Override
    public void update(Message message){
        try{
            socketOutput.writeObject(message);
        }catch(IOException e){
            if(logger != null) {
                logger.finest("Encountered an IO Exception in TCPHandler:\n" + e.getMessage() + "\n");
            }
        }
    }
}