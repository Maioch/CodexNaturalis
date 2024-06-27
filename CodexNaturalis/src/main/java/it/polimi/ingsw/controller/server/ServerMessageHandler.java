package it.polimi.ingsw.controller.server;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.core.EventHandler;
import it.polimi.ingsw.network.server.ExchangeHandlerManager;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.shared.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.shared.messages.setup.NewGameMessage;

import java.util.List;

/**
* Class that handles the messages sent by the clients and received by the server.
*
* @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
*/
public class ServerMessageHandler extends EventHandler<LabeledMessage> implements Runnable{

    //the server's ExchangeHandlerManager
    private final ExchangeHandlerManager exchangeHandlerManager;

    //the server's GamesManager.
    private final GamesManager games;

    /**
     * Constructor for the class.
     *
     * @param games                  the current list of games.
     * @param exchangeHandlerManager the exchange handler manager.
     *
     * @see GamesManager
     * @see ExchangeHandlerManager
     */
    public ServerMessageHandler(GamesManager games, ExchangeHandlerManager exchangeHandlerManager){
        super();
        this.exchangeHandlerManager = exchangeHandlerManager;
        this.games = games;
    }

    /**
     * Reads the message queue and handles the message according to its status.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        while(true){
            LabeledMessage labeledMessage = getEventFromQueue();
            if(labeledMessage == null){
                continue;
            }
            GameController currentClientGame = labeledMessage.exchangeHandler().getCurrentGame();
            if(labeledMessage.message().getStatus() == Status.REQUEST_PING){
                labeledMessage.exchangeHandler().update(new Message(Status.PING_ACK));
                continue;
            }
            if(labeledMessage.message().getStatus() == Status.PING_ACK) {
                exchangeHandlerManager.receivePing(labeledMessage.exchangeHandler());
                continue;
            }
            if(currentClientGame != null) {
                labeledMessage.exchangeHandler().getCurrentGame().addMessageToQueue(
                        labeledMessage.message(), labeledMessage.exchangeHandler());
                continue;
            }
            switch(labeledMessage.message().getStatus()){
                case REQUEST_GAMES -> {
                    List<GameInfo> matches = games.getFormattedAvailableMatches();
                    labeledMessage.exchangeHandler().update(new MatchListMessage(Status.REQUEST_GAMES, matches));
                }
                case NEW_GAME -> {
                    if (labeledMessage.message() instanceof NewGameMessage newGameMessage){
                        try{
                            int nameLength = newGameMessage.getName().length();
                            int gameId = games.addGame(newGameMessage.getNumberOfPlayers(),
                                    newGameMessage.getName().substring(0, Math.min(nameLength, Parameters.getMaxNameLength())));
                            labeledMessage.exchangeHandler().update(new IntegerMessage(Status.NEW_GAME, gameId));
                        }catch (IllegalNumberOfPlayers e) {
                            labeledMessage.exchangeHandler().update(new Message(Status.INVALID_PLAYERS_NUMBER));
                        }
                    }
                }
                case REQUEST_COLORS -> {
                    if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                        GameController game = games.getController(integerMessage.getValue());
                        if(game == null){
                            labeledMessage.exchangeHandler().update(new Message(Status.ERROR));
                            break;
                        }
                        game.addMessageToQueue(new Message(Status.REQUEST_COLORS), labeledMessage.exchangeHandler());
                    }
                }
                case JOIN_GAME -> {
                    if(labeledMessage.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getGameId());
                        if(game == null){
                            labeledMessage.exchangeHandler().update(new Message(Status.ERROR));
                            break;
                        }
                        game.addMessageToQueue(joinGameMessage, labeledMessage.exchangeHandler());
                    }
                }
                case RECONNECT -> {
                    if(labeledMessage.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getGameId());
                        if(game == null){
                            labeledMessage.exchangeHandler().update(new Message(Status.INVALID_RECONNECT));
                            break;
                        }
                        game.addMessageToQueue(new StringMessage(Status.RECONNECT, joinGameMessage.getNickname()), labeledMessage.exchangeHandler());
                        game.wakeUpAfterReconnect();
                    }
                }
            }
        }
    }
}