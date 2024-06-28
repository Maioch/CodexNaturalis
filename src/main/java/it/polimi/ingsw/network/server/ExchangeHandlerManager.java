package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.server.GameController;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.network.shared.ExchangeHandler;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Keeps track of all the handlers, handles disconnections,
 * and stops the handlers that are considered disconnected.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ExchangeHandlerManager {

    //stores all the currently running handlers.
    private final List<ExchangeHandler> handlers ;

    //stores the handlers which are expected to answer the ping request.
    private final List<ExchangeHandler> handlersToCheck;

    //stores the handlers which answered the ping request.
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
                handleDisconnections();
            }
        }, Parameters.getServerPingPeriodSeconds() * 1000L, Parameters.getServerPingPeriodSeconds() * 1000L);
    }

    /**
     * Adds a new handler to the handler list.
     *
     * @param handler the handler to add.
     *
     * @see ExchangeHandler
     */
    public void addHandler(ExchangeHandler handler){
        synchronized (handlers) {
            handlers.add(handler);
        }
    }

    /**
     * Adds the handler to the connected handlers and notifies that the ping has been received.
     *
     * @param handler the handler to add.
     *
     * @see ExchangeHandler
     */
    public void receivePing(ExchangeHandler handler){
        synchronized (connectedHandlers) {
            connectedHandlers.add(handler);
        }
    }

    /**
     * Checks for disconnected handlers, notifies the gameControllers about them,
     * then starts the next round of pings.
     */
    private void handleDisconnections(){
        synchronized (connectedHandlers) {
            synchronized (handlers) {
                List<ExchangeHandler> toDisconnect = new ArrayList<>();
                for (ExchangeHandler handler : handlers) {
                    if (!connectedHandlers.contains(handler) && handlersToCheck.contains(handler)) {
                        toDisconnect.add(handler);
                    }
                }
                List<GameController> gamesWithUsers =
                        handlers.stream().map(ExchangeHandler::getCurrentGame).filter(Objects::nonNull).distinct().toList();
                for(GameController game : gamesWithUsers){
                    List<ExchangeHandler> connectedToGame = connectedHandlers.stream()
                            .filter(h -> h.getCurrentGame() == game && !toDisconnect.contains(h))
                            .collect(Collectors.toCollection(ArrayList::new));
                    connectedToGame.addAll(handlers.stream()
                            .filter(h -> h.getCurrentGame() == game && !handlersToCheck.contains(h))
                            .toList());
                    List<ExchangeHandler> disconnected = handlers.stream()
                            .filter(h -> h.getCurrentGame() == game && toDisconnect.contains(h))
                            .toList();
                    game.handleDisconnections(connectedToGame, disconnected);
                }
                toDisconnect.forEach(ExchangeHandler::setDisconnected);
                connectedHandlers.clear();
                handlersToCheck.clear();
            }
        }
        synchronized (handlers) {
            for (ExchangeHandler handler : handlers) {
                handler.update(new Message(Status.REQUEST_PING));
                handlersToCheck.add(handler);
            }
        }
    }

    /**
     * Removes all disconnected handlers from the handler list and stops them.
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
