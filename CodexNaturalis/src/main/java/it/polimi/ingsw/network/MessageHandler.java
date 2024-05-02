package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

import java.util.LinkedList;
import java.util.Queue;

/**
* Abstract class that handles the messages by saving them in a queue
*
* @author Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli, Marco Maiocchi
*/
public abstract class MessageHandler implements Runnable{
    private final Queue<LabeledMessage> messageQueue;

    /**
     * Class constructor. It creates the message queue
     */
    public MessageHandler(){
        messageQueue = new LinkedList<>();
    }

    /**
     * Synchronized method that adds a message and the network handler that generated it to the queue
     * @param message the message to add
     * @param networkHandler the network handler to add
     */
    public synchronized void addMessageToQueue(Message message, NetworkHandler networkHandler){
        messageQueue.add(new LabeledMessage(networkHandler, message));
    }

    /**
     * Synchronized method that read the older message from the queue
     * @return the read labeled message
     */
    public synchronized LabeledMessage getMessageFromQueue(){
        return messageQueue.poll();
    }
}