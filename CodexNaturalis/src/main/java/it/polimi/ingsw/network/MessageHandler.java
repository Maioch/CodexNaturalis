package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.utilities.Pair;

import java.util.LinkedList;
import java.util.Queue;

public abstract class MessageHandler implements Runnable{
    private final Queue<Pair<NetworkHandler,Message>> messageQueue;

    public MessageHandler(){
        messageQueue = new LinkedList<>();
    }

    public synchronized void addMessageToQueue(Message message, NetworkHandler networkHandler){
        messageQueue.add(new Pair<>(networkHandler,message));
    }

    public synchronized Pair<NetworkHandler,Message> getMessageFromQueue(){
        return messageQueue.poll();
    }
}