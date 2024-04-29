package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameTakenException;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.utilities.Pair;

import java.util.*;

public class ClientMessageHandler implements Runnable{
    private final Queue<Pair<ClientHandler,Message>> messageQueue;

    public ClientMessageHandler(){
        messageQueue = new LinkedList<>();
    }

    public synchronized void addMessageToQueue(Message message, ClientHandler clientHandler){
        messageQueue.add(new Pair<>(clientHandler,message));
    }

    @Override
    public void run(){
        while(true) {
            synchronized (messageQueue) {
                if (!messageQueue.isEmpty()) {
                    Pair<ClientHandler, Message> messagePair = messageQueue.poll();
                    GameController currentClientGame = messagePair.getKey().getCurrentGame();
                    if (currentClientGame != null) {
                        messagePair.getKey().getCurrentGame().addMessageToQueue(messagePair.getValue(), messagePair.getKey());
                    }else{
                        switch (messagePair.getValue().getStatus()) {
                            case SHOW_MATCHES -> {
                                HashMap<Integer, String> matches = Server.getFormattedMatches();
                                messagePair.getKey().update(new MatchListMessage(matches));
                            }
                            case NEW_GAME -> {
                                if (messagePair.getValue() instanceof NewGameMessage newGameMessage) {
                                    try {
                                        int id = Server.addMatch(newGameMessage.getNumberOfPlayers(), newGameMessage.getName());
                                        messagePair.getKey().update(new IntegerMessage(Status.NEW_GAME, id));
                                    } catch (IllegalNumberOfPlayers e) {
                                        messagePair.getKey().update(new Message(Status.NEW_GAME_FAIL));
                                    }
                                }
                            }
                            case REQUEST_COLOR -> {
                                if (messagePair.getValue() instanceof IntegerMessage integerMessage) {
                                    GameController game = Server.getMatch(integerMessage.getValue());
                                    if (game != null) {
                                        messagePair.getKey().update(new ContentMessage(Status.REQUEST_COLOR, game.requestColors()));
                                    } else {
                                        messagePair.getKey().update(new Message(Status.ERROR));
                                    }
                                }
                            }
                            case JOIN_GAME -> {
                                if (messagePair.getValue() instanceof JoinGameMessage joinGameMessage) {
                                    GameController game = Server.getMatch(joinGameMessage.getRoomId());
                                    if (game != null) {
                                        try {
                                            game.acceptPlayer(joinGameMessage.getNickname(),
                                                    joinGameMessage.getColor(),
                                                    messagePair.getKey());
                                            messagePair.getKey().setCurrentGame(game);
                                        } catch (GameFullException f) {
                                            messagePair.getKey().update(new Message(Status.GAME_FULL));
                                        } catch (GameException | NicknameTakenException e) {
                                            messagePair.getKey().update(new Message(Status.ERROR));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}