package it.polimi.ingsw.network.server;

import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.network.shared.ExchangeHandler;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Keeps track of all the handlers, handles disconnections when a handler isn't part of a game,
 * and stops the handlers that are considered disconnected.
 */
public class ExchangeHandlerManager {

    private final List<ExchangeHandler> handlers ;
    private final List<ExchangeHandler> handlersToCheck;
    private final List<ExchangeHandler> connectedHandlers;

    /**
     * Class constructor.
     */
    public ExchangeHandlerManager(){
        handlers = new ArrayList<>();
        handlersToCheck = new ArrayList<>();
        connectedHandlers = new ArrayList<>();
        Timer pingTimer = new Timer();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                stopDisconnectedHandlers();
                handleSetupDisconnections();
            }
        }, Parameters.getServerPingPeriodSeconds() * 1000L, Parameters.getServerPingPeriodSeconds() * 1000L);
    }

    /**
     * Adds a new handler to the handler list.
     *
     * @param handler the handler to add.
     */
    public void addHandler(ExchangeHandler handler){
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    /**
     * Adds the handler to the connected handler list and notifies that the ping has been received.
     *
     * @param handler the handler to add.
     */
    public void receivePing(ExchangeHandler handler){
        synchronized (connectedHandlers) {
            connectedHandlers.add(handler);
        }
        System.out.println("received ping");
    }

    /**
     * Handles the disconnection of a handler during the setup phase of the game.
     */
    private void handleSetupDisconnections(){
        synchronized (connectedHandlers) {
            for(ExchangeHandler handler : handlersToCheck) {
                if (!connectedHandlers.contains(handler)) {
                    handler.setDisconnected();
                }
            }
            connectedHandlers.clear();
            handlersToCheck.clear();
        }
        synchronized (handlers) {
            for (ExchangeHandler handler : handlers) {
                if (handler.getCurrentGame() == null) {
                    handlersToCheck.add(handler);
                }
            }
        }
        for (ExchangeHandler handler : handlersToCheck) {
            handler.update(new Message(Status.REQUEST_PING));
        }
    }

    /**
     * Removes all disconnected handlers from the handlers list and stops them.
     */
    private void stopDisconnectedHandlers() {
        synchronized (handlers) {
            handlers.removeIf(h -> {
                if(h.isDisconnected()){
                    h.stop();
                    return true;
                }
                return false;
            });
        }
    }
}
