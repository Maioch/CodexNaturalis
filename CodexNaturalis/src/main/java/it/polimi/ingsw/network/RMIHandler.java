package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.server.ServerMessageHandler;

import java.io.IOException;

public class RMIHandler extends NetworkHandler{

    public RMIHandler(ServerMessageHandler handler) throws IOException{
        super(handler);
    }

    @Override
    public void run(){

    }

    @Override
    public void update(Message message){

    }
}