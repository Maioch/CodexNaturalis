package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

import java.io.IOException;

public class RMIHandler extends NetworkHandler{

    public RMIHandler(MessageHandler handler) throws IOException{
        super(handler);
    }

    @Override
    public void run(){

    }

    @Override
    public void update(Message message){

    }
}