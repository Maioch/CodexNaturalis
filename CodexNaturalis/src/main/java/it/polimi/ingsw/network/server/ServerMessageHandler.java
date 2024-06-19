package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.server.GameController;
import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.controller.server.GamesManager;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.network.shared.EventHandler;
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
* @author Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli, Marco Maiocchi
*/
public class ServerMessageHandler extends EventHandler<LabeledMessage> implements Runnable{
    private final GamesManager games;

    /**
     * Constructor for the class.
     * @param games the current list of games.
     */
    public ServerMessageHandler(GamesManager games){
        super();
        this.games = games;
    }

    /**
     * Overridden run method. It reads the message queue and handles the message according to its status.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        while(true){
            LabeledMessage labeledMessage = getEventFromQueue();
            if(labeledMessage == null){
                continue;
            }
            GameController currentClientGame = labeledMessage.networkHandler().getCurrentGame();
            if(labeledMessage.message().getStatus() == Status.REQUEST_PING){
                labeledMessage.networkHandler().update(new Message(Status.PING_ACK));
                continue;
            }
            if(currentClientGame != null) {
                if(labeledMessage.message().getStatus() == Status.PING_ACK){
                    labeledMessage.networkHandler().getCurrentGame().receivePing(labeledMessage.networkHandler());
                } else {
                    labeledMessage.networkHandler().getCurrentGame().addMessageToQueue(
                            labeledMessage.message(), labeledMessage.networkHandler());
                }
                continue;
            }
            switch(labeledMessage.message().getStatus()){
                case REQUEST_GAMES -> {
                    List<GameInfo> matches = games.getFormattedAvailableMatches();
                    labeledMessage.networkHandler().update(new MatchListMessage(Status.REQUEST_GAMES, matches));
                }
                case NEW_GAME -> {
                    if (labeledMessage.message() instanceof NewGameMessage newGameMessage){
                        try{
                            int nameLength = newGameMessage.getName().length();
                            int gameId = games.addGame(newGameMessage.getNumberOfPlayers(),
                                    newGameMessage.getName().substring(0, Math.min(nameLength,GameParameters.getMaxNicknameLength())));
                            labeledMessage.networkHandler().update(new IntegerMessage(Status.NEW_GAME, gameId));
                        }catch (IllegalNumberOfPlayers e) {
                            List<GameInfo> matches = games.getFormattedAvailableMatches();
                            labeledMessage.networkHandler().update(new MatchListMessage(Status.INVALID_PLAYERS_NUMBER, matches));
                        }
                    }
                }
                case REQUEST_COLORS -> {
                    if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                        GameController game = games.getController(integerMessage.getValue());
                        if(game == null){
                            labeledMessage.networkHandler().update(new Message(Status.ERROR));
                            break;
                        }
                        game.addMessageToQueue(new Message(Status.REQUEST_COLORS), labeledMessage.networkHandler());
                    }
                }
                case JOIN_GAME -> {
                    if(labeledMessage.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getGameId());
                        if(game == null){
                            labeledMessage.networkHandler().update(new Message(Status.ERROR));
                            break;
                        }
                        game.addMessageToQueue(joinGameMessage, labeledMessage.networkHandler());
                    }
                }
                case RECONNECT -> {
                    if(labeledMessage.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getGameId());
                        if(game == null){
                            labeledMessage.networkHandler().update(new Message(Status.INVALID_RECONNECT));
                            break;
                        }
                        game.addMessageToQueue(new StringMessage(Status.RECONNECT, joinGameMessage.getNickname()), labeledMessage.networkHandler());
                        game.wakeUpAfterReconnect();
                    }
                }
            }
        }
    }
}