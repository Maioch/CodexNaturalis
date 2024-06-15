package it.polimi.ingsw;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.EventHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class TestNetworkHandler extends NetworkHandler {
    private final List<Message> receivedMessages;
    private final GameController controller;

    public TestNetworkHandler(GameController controller){
        super(new EventHandler<>() {
            @Override
            public void run() {

            }
        });
        receivedMessages = new ArrayList<>();
        this.controller = controller;
    }

    public TestNetworkHandler(){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.controller = null;
    }

    public List<Message> getReceivedMessages() {
        List<Message> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    public void send(Message message){
        assertNotNull(controller);
        controller.addMessageToQueue(message, this);
    }

    @Override
    public void update(Message message){
        receivedMessages.add(message);
    }

    @Override
    public void stop(){}
}