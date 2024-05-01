package it.polimi.ingsw.network.client;

import it.polimi.ingsw.client.model.ClientGame;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.utilities.Pair;

public class ClientMessageHandler extends MessageHandler{
    ClientGame game;

    @Override
    public void run(){
        while(true){
            Pair<NetworkHandler, Message> messagePair = getMessageFromQueue();
            if(messagePair == null){
                continue;
            }
        }
    }

    public void setGame(ClientGame game){
        this.game = game;
    }
}