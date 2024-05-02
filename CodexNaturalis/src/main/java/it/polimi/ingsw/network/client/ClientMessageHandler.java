package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.client.ClientGame;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.generic.ContentMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.MatchListMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

public class ClientMessageHandler extends MessageHandler{
    ClientGame game;
    SetupView setupView;
    GameView gameView;
    NetworkHandler networkHandler;

    public ClientMessageHandler(ClientGame game, SetupView setupView) {
        this.game = game;
        this.setupView = setupView;
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
                            setupView.updateMatchList(matchListMessage.getMatchList());
                        }
                    }
                    case NEW_GAME -> {
                        if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                            setupView.newGameSuccess(integerMessage.getValue());
                            networkHandler.update(new IntegerMessage(Status.REQUEST_COLOR,integerMessage.getValue()));
                        }
                    }
                    case REQUEST_COLOR -> {
                        if(labeledMessage.message() instanceof ContentMessage contentMessage){
                            setupView.showJoinGameDialog(contentMessage.getContent());
                        }
                    }
                    case JOIN_GAME_OK -> {

                        //game = new ClientGame(new LocalPlayer())
                    }
                }
                continue;
            }
            //manage game-related messages
        }
    }

    public synchronized void setGameView(GameView gameView){
        this.gameView = gameView;
    }
}