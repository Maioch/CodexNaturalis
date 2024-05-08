package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;

import java.util.HashMap;
import java.util.Map;

public class ServerSubject {
    private final Map<String, NetworkHandler> networkHandlers;

    public ServerSubject(){
        networkHandlers = new HashMap<>();
    }

    public synchronized void subscribe(String nickname, NetworkHandler networkHandler){
        networkHandlers.put(nickname, networkHandler);
    }


    public synchronized void unsubscribe(String nickname){
        networkHandlers.remove(nickname);
    }

    public synchronized void notifyAll(Message message){
        for(NetworkHandler networkHandler : networkHandlers.values()){
            networkHandler.update(message);
        }
    }

    public synchronized void notify(String nickname, Message message){
        if(networkHandlers.containsKey(nickname)){
            networkHandlers.get(nickname).update(message);
        }
    }

    public synchronized NetworkHandler getNetworkHandler(String nickname){
        return networkHandlers.get(nickname);
    }
}