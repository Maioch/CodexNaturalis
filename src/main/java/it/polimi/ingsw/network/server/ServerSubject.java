package it.polimi.ingsw.network.server;

import it.polimi.ingsw.network.shared.ExchangeHandler;
import it.polimi.ingsw.network.shared.messages.Message;

import java.util.HashMap;
import java.util.Map;

/**
 * Implements the observer pattern by interacting with the exchange handlers, notifying them about changes
 * in the game model.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see ExchangeHandler
 */
public class ServerSubject {

    //stores the nicknames used to identify the players along with its exchangeHandlers
    private final Map<String, ExchangeHandler> exchangeHandlers;

    /**
     * Constructor for the class.
     */
    public ServerSubject(){
        exchangeHandlers = new HashMap<>();
    }

    /**
     * Adds a new client handler to the map.
     *
     * @param nickname       the nickname of the player associated with the new client.
     * @param exchangeHandler the object that handles the new client.
     *
     * @see ExchangeHandler
     */
    public synchronized void subscribe(String nickname, ExchangeHandler exchangeHandler){
        exchangeHandlers.put(nickname, exchangeHandler);
    }

    /**
     * Removes a client from the map.
     *
     * @param nickname the nickname of the player associated with the client.
     */
    public synchronized void unsubscribe(String nickname){
        exchangeHandlers.remove(nickname);
    }

    /**
     * Notifies all the clients present in the map.
     *
     * @param message the message to send them.
     *
     * @see Message
     */
    public synchronized void notifyAll(Message message){
        for(ExchangeHandler exchangeHandler : exchangeHandlers.values()){
            if(!exchangeHandler.isDisconnected()) {
                exchangeHandler.update(message);
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
        if(exchangeHandlers.containsKey(nickname) && !exchangeHandlers.get(nickname).isDisconnected()){
            exchangeHandlers.get(nickname).update(message);
        }
    }

    /**
     * Gets the corresponding ExchangeHandler.
     *
     * @param nickname the nickname of the player associated with the client (ExchangeHandler).
     *
     * @return         the handler associated to the client.
     *
     * @see ExchangeHandler
     */
    public synchronized ExchangeHandler getExchangeHandler(String nickname){
        return exchangeHandlers.get(nickname);
    }
}