package it.polimi.ingsw;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.network.EventHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class TestNetworkHandler extends NetworkHandler {
    private final List<Message> receivedMessages;
    private final GameController controller;
    private boolean stopped;

    public TestNetworkHandler(GameController controller){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.controller = controller;
        this.stopped = false;
    }

    public TestNetworkHandler(){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.controller = null;
    }

    public synchronized List<Message> getReceivedMessages() {
        List<Message> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    public void send(Message message){
        assertNotNull(controller);
        assertFalse(stopped);
        controller.addMessageToQueue(message, this);
    }

    public Message awaitForMessage(Status expectedStatus, int waitDurationMilliseconds, List<Status> ignoredStatus){
        try {
            Thread.sleep(waitDurationMilliseconds);
        } catch (InterruptedException e) {
            fail();
        }
        Message message;
        synchronized (this) {
            if(receivedMessages.isEmpty()){
                fail();
            }
            do {
                message = receivedMessages.removeLast();
            } while (ignoredStatus.contains(message.getStatus()) && !receivedMessages.isEmpty());
        }
        assertEquals(expectedStatus, message.getStatus());
        return message;
    }

    public Message awaitForMessage(Status expectedStatus, int waitDurationMilliseconds){
        return awaitForMessage(expectedStatus, waitDurationMilliseconds, new ArrayList<>());
    }

    public boolean removeIfStatus(Status expectedStatus, int waitDurationMilliseconds) {
        try {
            Thread.sleep(waitDurationMilliseconds);
        } catch (InterruptedException e) {
            fail();
        }
        synchronized (this){
            return receivedMessages.removeIf(m -> m.getStatus().equals(expectedStatus));
        }
    }

    @Override
    public synchronized void update(Message message){
        if(stopped){
            return;
        }
        if(message.getStatus() == Status.REQUEST_PING){
            assertNotNull(controller);
            controller.receivePing(this);
            return;
        }
        receivedMessages.add(message);
    }

    @Override
    public synchronized void stop(){
        stopped = true;
    }
}