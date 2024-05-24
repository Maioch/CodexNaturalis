package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Class that implements the observer pattern by interacting with the server listeners, notifying them about changes
 * in the game model.
 */
public class ServerSubject {
    private final Map<String, NetworkHandler> networkHandlers;

    /**
     * Constructor for the class.
     */
    public ServerSubject(){
        networkHandlers = new HashMap<>();
    }

    /**
     * Method used to add a new client handler to the map.
     * @param nickname the nickname of the player associated with the new client.
     * @param networkHandler the object that handles the new client.
     */
    public synchronized void subscribe(String nickname, NetworkHandler networkHandler){
        networkHandlers.put(nickname, networkHandler);
    }

    /**
     * Method used to remove a client from the map.
     * @param nickname the nickname of the player associated with the client.
     */
    public synchronized void unsubscribe(String nickname){
        networkHandlers.remove(nickname);
    }

    /**
     * Method used to notify all the clients present in the map.
     * @param message the message to send them.
     */
    public synchronized void notifyAll(Message message){
        for(NetworkHandler networkHandler : networkHandlers.values()){
            if(!networkHandler.isDisconnected()) {
                networkHandler.update(message);
            }
        }
    }

    /**
     * Method used to notify a single client present in the map.
     * @param nickname the nickname of the player associated with the client.
     * @param message the message to send them.
     */
    public synchronized void notify(String nickname, Message message){
        if(networkHandlers.containsKey(nickname) && !networkHandlers.get(nickname).isDisconnected()){
            networkHandlers.get(nickname).update(message);
        }
    }

    /**
     * @param nickname the nickname of the player associated with the client.
     * @return the handler associated to the client.
     */
    public synchronized NetworkHandler getNetworkHandler(String nickname){
        return networkHandlers.get(nickname);
    }
}