package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.client.ClientGame;
import it.polimi.ingsw.model.client.LocalPlayer;
import it.polimi.ingsw.model.client.RemotePlayer;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.CardHandMessage;
import it.polimi.ingsw.network.messages.game.DrawOptionsMessage;
import it.polimi.ingsw.network.messages.generic.ContentMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.messages.setup.PlayerMessage;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

public class ClientMessageHandler extends MessageHandler{
    ClientGame game;
    SetupView setupView;
    GameView gameView;
    NetworkHandler networkHandler;
    EventSubmitter eventSubmitter;

    public ClientMessageHandler(ClientGame game, SetupView setupView, EventSubmitter eventSubmitter) {
        this.game = game;
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
    }

    @Override
    public void run(){
        while(true){
            LabeledMessage labeledMessage = getMessageFromQueue();
            if(labeledMessage == null){
                continue;
            }
            if(game == null){
                switch (labeledMessage.message().getStatus()){
                    case SHOW_MATCHES -> {
                        if(labeledMessage.message() instanceof MatchListMessage matchListMessage){
                            eventSubmitter.submit(() -> setupView.updateMatchList(matchListMessage.getMatchList()));
                        }
                    }
                    case NEW_GAME -> {
                        if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                            eventSubmitter.submit(() -> setupView.newGameSuccess(integerMessage.getValue()));
                            networkHandler.update(new IntegerMessage(Status.REQUEST_COLOR, integerMessage.getValue()));
                        }
                    }
                    case SEND_COLOR -> {
                        if(labeledMessage.message() instanceof ContentMessage contentMessage){
                            if (contentMessage.getContent().isEmpty()){
                                eventSubmitter.submit(() -> setupView.showCriticalError(Status.GAME_FULL.getMessage()));
                            }
                            else{
                                eventSubmitter.submit(() -> setupView.showJoinGameDialog(contentMessage.getContent()));
                            }
                        }
                    }
                    case JOIN_GAME -> {
                        if(labeledMessage.message() instanceof PlayerMessage playerMessage){
                            eventSubmitter.submit(() -> setupView.showSuccessfulJoin());
                            game = new ClientGame(new LocalPlayer(playerMessage.getNickname(), playerMessage.getColor()));
                        }
                    }
                    case GAME_FULL ->  {
                        eventSubmitter.submit(() -> setupView.showCriticalError(Status.GAME_FULL.getMessage()));
                    }
                    case REQUEST_USERNAME -> {
                        eventSubmitter.submit(() -> setupView.showUserError(Status.REQUEST_USERNAME.getMessage()));
                    }
                    case REQUEST_COLOR -> {
                        eventSubmitter.submit(() -> setupView.showUserError(Status.REQUEST_COLOR.getMessage()));
                    }
                }
                continue;
            }
            switch (labeledMessage.message().getStatus()){
                case NEW_PLAYER_JOINED -> {
                    if(labeledMessage.message() instanceof PlayerMessage playerMessage) {
                        boolean isPlayerMissing = game.getRemotePlayers().stream()
                                .map(RemotePlayer::getNickname)
                                .anyMatch(n -> n.equals(playerMessage.getNickname()));
                        if (isPlayerMissing) {
                            game.addRemotePlayer(new RemotePlayer(playerMessage.getNickname(), playerMessage.getColor()));
                        }
                    }
                }
                case TURN_NOTIFICATION -> {
                    if(labeledMessage.message() instanceof StringMessage stringMessage){
                        eventSubmitter.submit(() -> gameView.turnChanged(stringMessage.getString()));
                    }
                }
                case DRAW_OPTIONS -> {
                    if(labeledMessage.message() instanceof DrawOptionsMessage drawOptionsMessage){
                        eventSubmitter.submit(() -> gameView.updateDrawableCards(drawOptionsMessage.getDrawableOptions()));
                    }
                }
                case STARTER_CARD -> {
                    if(labeledMessage.message() instanceof CardHandMessage cardHandMessage){
                        eventSubmitter.submit(() -> gameView.requestStarterSide(cardHandMessage.getCardHand()));
                    }
                }

            }
            //manage game-related messages
        }
    }

    public synchronized void setGameView(GameView gameView){
        this.gameView = gameView;
    }
}