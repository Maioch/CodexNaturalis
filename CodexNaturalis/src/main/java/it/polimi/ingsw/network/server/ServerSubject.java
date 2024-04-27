package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.messages.Message;

import java.util.ArrayList;
import java.util.HashMap;

public abstract class ServerSubject {
    private final HashMap<String,ServerListener> listeners;

    public ServerSubject(){
        listeners = new HashMap<>();
    }

    public void subscribe(String nickname, ServerListener listener){
        listeners.put(nickname,listener);
    }

    public void unsubscribe(String nickname){
        listeners.remove(nickname);
    }

    public void notifyAll(Message message){
        for(ServerListener listener : listeners.values()){
            listener.update(message);
        }
    }

    public void notify(Message message, String nickname){
        listeners.get(nickname).update(message);
    }
}
