package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.controller.GamesManager;
import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameTakenException;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.setup.GameColorsMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.messages.setup.NewGameMessage;

import java.util.*;

/**
* Class that handles the messages received by the server
*
* @author Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli, Marco Maiocchi
*/
public class ServerMessageHandler extends MessageHandler implements Runnable{
    private final GamesManager games;

    /**
     * Class constructor.
     * @param games represents the current list of games
     */
    public ServerMessageHandler(GamesManager games){
        super();
        this.games = games;
    }

    /**
     * Overridden run method. It reads the message queue and handles the message according to its status
     */
    @Override
    public void run(){
        while(true){
            LabeledMessage labeledMessage = getMessageFromQueue();
            if(labeledMessage == null){
                continue;
            }
            GameController currentClientGame = labeledMessage.networkHandler().getCurrentGame();
            if(currentClientGame != null) {
                labeledMessage.networkHandler().getCurrentGame().addMessageToQueue(labeledMessage.message(), labeledMessage.networkHandler());
                continue;
            }
            switch(labeledMessage.message().getStatus()){
                case REQUEST_GAMES -> {
                    HashMap<Integer, String> matches = games.getFormattedAvailableMatches();
                    labeledMessage.networkHandler().update(new MatchListMessage(Status.REQUEST_GAMES, matches));
                }
                case NEW_GAME -> {
                    if (labeledMessage.message() instanceof NewGameMessage newGameMessage){
                        try{
                            int gameId = games.addGame(newGameMessage.getNumberOfPlayers(),
                                    newGameMessage.getName().substring(0, GameParameters.getMaxNicknameLength()));
                            labeledMessage.networkHandler().update(new IntegerMessage(Status.NEW_GAME, gameId));
                        }catch (IllegalNumberOfPlayers e) {
                            HashMap<Integer, String> matches = games.getFormattedAvailableMatches();
                            labeledMessage.networkHandler().update(new MatchListMessage(Status.INVALID_PLAYERS_NUMBER, matches));
                        }
                    }
                }
                case REQUEST_COLORS -> {
                    if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                        GameController game = games.getController(integerMessage.getValue());
                        labeledMessage.networkHandler().update(new GameColorsMessage(Status.REQUEST_COLORS,
                                game != null ? game.requestColors() : new ArrayList<>(), integerMessage.getValue()));
                    }
                }
                case JOIN_GAME -> {
                    if(labeledMessage.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getGameId());
                        if(game == null){
                            labeledMessage.networkHandler().update(new Message(Status.ERROR));
                            break;
                        }
                        try{
                            game.acceptPlayer(joinGameMessage.getNickname().substring(0, GameParameters.getMaxNicknameLength()),
                                    joinGameMessage.getColor(),
                                    labeledMessage.networkHandler());
                            labeledMessage.networkHandler().setCurrentGame(game);
                        }catch(GameFullException f){
                            labeledMessage.networkHandler().update(new Message(Status.GAME_FULL));
                        }catch(NicknameTakenException e){
                            labeledMessage.networkHandler().update(new IntegerMessage(Status.INVALID_NICKNAME, joinGameMessage.getGameId()));
                        }catch(GameException e){
                            labeledMessage.networkHandler().update(new IntegerMessage(Status.INVALID_COLOR, joinGameMessage.getGameId()));
                        }
                    }
                }
            }
        }
    }
}