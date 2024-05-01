package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameTakenException;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.utilities.Pair;

import java.util.*;

public class ServerMessageHandler extends MessageHandler implements Runnable{
    private final GamesManager games;

    public ServerMessageHandler(GamesManager games){
        super();
        this.games = games;
    }

    @Override
    public void run(){
        while(true){
            Pair<NetworkHandler, Message> messagePair;
            messagePair = getMessageFromQueue();
            if(messagePair == null){
                continue;
            }
            GameController currentClientGame = messagePair.getKey().getCurrentGame();
            if(currentClientGame != null) {
                messagePair.getKey().getCurrentGame().addMessageToQueue(messagePair.getValue(), messagePair.getKey());
                continue;
            }
            switch(messagePair.getValue().getStatus()){
                case SHOW_MATCHES -> {
                    HashMap<Integer, String> matches = games.getFormattedMatches();
                    messagePair.getKey().update(new MatchListMessage(matches));
                }
                case NEW_GAME -> {
                    if (messagePair.getValue() instanceof NewGameMessage newGameMessage) {
                        try{
                            int gameId = games.addGame(newGameMessage.getNumberOfPlayers(), newGameMessage.getName());
                            messagePair.getKey().update(new IntegerMessage(Status.NEW_GAME, gameId));
                        }catch (IllegalNumberOfPlayers e) {
                            messagePair.getKey().update(new Message(Status.NEW_GAME_FAIL));
                        }
                    }
                }
                case REQUEST_COLOR -> {
                    if(messagePair.getValue() instanceof IntegerMessage integerMessage){
                        GameController game = games.getController(integerMessage.getValue());
                        messagePair.getKey().update(game != null ?
                                new ContentMessage(Status.REQUEST_COLOR, game.requestColors()) :
                                new Message(Status.ERROR));
                    }
                }
                case JOIN_GAME -> {
                    if(messagePair.getValue() instanceof JoinGameMessage joinGameMessage){
                        GameController game = games.getController(joinGameMessage.getRoomId());
                        if(game == null) {
                            break;
                        }
                        try{
                            game.acceptPlayer(joinGameMessage.getNickname(),
                                    joinGameMessage.getColor(),
                                    messagePair.getKey());
                            messagePair.getKey().setCurrentGame(game);
                        }catch(GameFullException f){
                            messagePair.getKey().update(new Message(Status.GAME_FULL));
                        }catch(NicknameTakenException e){
                            messagePair.getKey().update(new Message(Status.REQUEST_USERNAME));
                        }catch(GameException e){
                            messagePair.getKey().update(new Message(Status.ERROR));
                        }
                    }
                }
            }
        }
    }
}