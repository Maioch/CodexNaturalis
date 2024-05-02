package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameTakenException;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.generic.ContentMessage;
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
            LabeledMessage messagePair;
            messagePair = getMessageFromQueue();
            if(messagePair == null){
                continue;
            }
            GameController currentClientGame = messagePair.networkHandler().getCurrentGame();
            if(currentClientGame != null) {
                messagePair.networkHandler().getCurrentGame().addMessageToQueue(messagePair.message(), messagePair.networkHandler());
                continue;
            }
            switch(messagePair.message().getStatus()){
                case SHOW_MATCHES -> {
                    HashMap<Integer, String> matches = games.getFormattedAvailableMatches();
                    messagePair.networkHandler().update(new MatchListMessage(matches));
                }
                case NEW_GAME -> {
                    if (messagePair.message() instanceof NewGameMessage newGameMessage) {
                        try{
                            int gameId = games.addGame(newGameMessage.getNumberOfPlayers(), newGameMessage.getName());
                            messagePair.networkHandler().update(new IntegerMessage(Status.NEW_GAME, gameId));
                        }catch (IllegalNumberOfPlayers e) {
                            messagePair.networkHandler().update(new Message(Status.NEW_GAME_FAIL));
                        }
                    }
                }
                case REQUEST_COLOR -> {
                    if(messagePair.message() instanceof IntegerMessage integerMessage){
                        GameController game = games.getController(integerMessage.getValue());
                        messagePair.networkHandler().update(game != null ?
                                new ContentMessage(Status.REQUEST_COLOR, game.requestColors()) :
                                new Message(Status.ERROR));
                    }
                }
                case JOIN_GAME -> {
                    if(messagePair.message() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getRoomId());
                        if(game == null) {
                            break;
                        }
                        try{
                            game.acceptPlayer(joinGameMessage.getNickname(),
                                    joinGameMessage.getColor(),
                                    messagePair.networkHandler());
                            messagePair.networkHandler().setCurrentGame(game);
                            messagePair.networkHandler().update(new Message(Status.JOIN_GAME_OK));
                        }catch(GameFullException f){
                            messagePair.networkHandler().update(new Message(Status.GAME_FULL));
                        }catch(NicknameTakenException e){
                            messagePair.networkHandler().update(new Message(Status.REQUEST_USERNAME));
                        }catch(GameException e){
                            messagePair.networkHandler().update(new Message(Status.ERROR));
                        }
                    }
                }
            }
        }
    }
}