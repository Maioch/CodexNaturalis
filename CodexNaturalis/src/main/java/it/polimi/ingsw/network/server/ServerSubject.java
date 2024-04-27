package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.messages.Message;

import java.util.ArrayList;

public abstract class ServerSubject {
    private final ArrayList<ServerListener> listeners;

    public ServerSubject(){
        listeners = new ArrayList<>();
    }

    public void subscribe(ServerListener listener){
        listeners.add(listener);
    }

    public void unsubscribe(ServerListener listener){
        listeners.remove(listener);
    }

    public void notify(Message message){
        for(ServerListener listener : listeners){
            listener.update(message);
        }
    }
}
