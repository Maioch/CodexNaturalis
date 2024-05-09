package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.server.RMIHandlerProvider;

import java.io.IOException;
import java.rmi.RemoteException;

public class RMIHandler extends NetworkHandler implements RMIInterface{
    private RMIInterface clientInterface;

    public RMIHandler(MessageHandler handler) throws IOException{
        super(handler);
        clientInterface = null;
    }

    public void receiveUpdate(Message message) throws RemoteException {
        handler.addMessageToQueue(message,this);
    }

    public void setCallback(RMIInterface clientInterface){
        this.clientInterface = clientInterface;
    }

    @Override
    public void update(Message message){
        try {
            clientInterface.receiveUpdate(message);
        }catch (RemoteException e){
            System.out.println(e.getMessage());
        }
    }
}