package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.shared.NetworkHandler;
import it.polimi.ingsw.network.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the observer pattern by interacting with the network handlers, notifying them about changes
 * in the game model.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see NetworkHandler
 */
public class ServerSubject {

    //stores the nicknames used to identify the players along with its networkHandlers
    private final Map<String, NetworkHandler> networkHandlers;

    /**
     * Constructor for the class.
     */
    public ServerSubject(){
        networkHandlers = new HashMap<>();
    }

    /**
     * Adds a new client handler to the map.
     *
     * @param nickname       the nickname of the player associated with the new client.
     * @param networkHandler the object that handles the new client.
     *
     * @see NetworkHandler
     */
    public synchronized void subscribe(String nickname, NetworkHandler networkHandler){
        networkHandlers.put(nickname, networkHandler);
    }

    /**
     * Removes a client from the map.
     *
     * @param nickname the nickname of the player associated with the client.
     */
    public synchronized void unsubscribe(String nickname){
        networkHandlers.remove(nickname);
    }

    /**
     * Notifies all the clients present in the map.
     *
     * @param message the message to send them.
     *
     * @see Message
     */
    public synchronized void notifyAll(Message message){
        for(NetworkHandler networkHandler : networkHandlers.values()){
            if(!networkHandler.isDisconnected()) {
                networkHandler.update(message);
            }
        }
    }

    /**
     * Notifies a single client present in the map.
     *
     * @param nickname the nickname of the player associated with the client.
     * @param message  the message to send them.
     *
     * @see Message
     */
    public synchronized void notify(String nickname, Message message){
        if(networkHandlers.containsKey(nickname) && !networkHandlers.get(nickname).isDisconnected()){
            networkHandlers.get(nickname).update(message);
        }
    }

    /**
     * Gets the corresponding NetworkHandler.
     *
     * @param nickname the nickname of the player associated with the client (NetworkHandler).
     *
     * @return         the handler associated to the client.
     *
     * @see NetworkHandler
     */
    public synchronized NetworkHandler getNetworkHandler(String nickname){
        return networkHandlers.get(nickname);
    }
}