package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.messages.Message;

import java.util.ArrayList;
import java.util.HashMap;

public class ServerSubject {
    private final HashMap<String,ServerListener> listeners;

    public ServerSubject(){
        listeners = new HashMap<>();
    }

    public synchronized void subscribe(String nickname, ServerListener listener){
        listeners.put(nickname,listener);
    }

    public synchronized void unsubscribe(String nickname){
        listeners.remove(nickname);
    }

    public synchronized void notifyAll(Message message){
        for(ServerListener listener : listeners.values()){
            listener.update(message);
        }
    }

    public synchronized void notify(String nickname, Message message){
        if(listeners.containsKey(nickname)){
            listeners.get(nickname).update(message);
        }
    }

    public synchronized ServerListener getListener(String nickname){
        return listeners.get(nickname);
    }
}