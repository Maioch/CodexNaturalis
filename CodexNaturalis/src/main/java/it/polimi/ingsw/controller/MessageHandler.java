package it.polimi.ingsw.controller;

import it.polimi.ingsw.network.messages.ContentMessage;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.server.ClientHandler;

import java.util.HashMap;
import java.util.Queue;

public class MessageHandler implements Runnable{
    private Queue<Message> messageQueue;
    private final HashMap<String, ClientHandler> clients;
    private GameController gameController;

    public MessageHandler(){

        clients = new HashMap<>();
    }

    public synchronized void addClient(ClientHandler client){
        clients.put()
    }

    public synchronized void addMessageToQueue(Message message){
        messageQueue.add(message);
    }

    @Override
    public void run(){
        while(!gameController.isGameEnded()){
            if(!messageQueue.isEmpty()){
                Message message = messageQueue.poll();
                if(message instanceof ContentMessage){
                    ContentMessage contentMessage = (ContentMessage) message;
                    switch (message.getStatus()){
                        case SEND_COLOR -> gameController.requestColors(contentMessage.getContent());
                    }
                }
            }
        }
    }
}
