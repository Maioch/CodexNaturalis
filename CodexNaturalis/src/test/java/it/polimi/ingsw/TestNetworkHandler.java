package it.polimi.ingsw;

import it.polimi.ingsw.controller.client.ClientController;
import it.polimi.ingsw.controller.server.GameController;
import it.polimi.ingsw.network.shared.EventHandler;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.NetworkHandler;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class TestNetworkHandler extends NetworkHandler {

    private final List<Message> receivedMessages;
    private final GameController gameController;
    private final ClientController clientController;
    private boolean stopped;

    public TestNetworkHandler(GameController gameController){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.gameController = gameController;
        this.clientController = null;
        this.stopped = false;
    }

    public TestNetworkHandler(ClientController clientController){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.gameController = null;
        this.clientController = clientController;
    }

    public TestNetworkHandler(){
        super(new EventHandler<>() {
            @Override
            public void run(){}
        });
        receivedMessages = new ArrayList<>();
        this.gameController = null;
        this.clientController = null;
    }

    public synchronized List<Message> getReceivedMessages() {
        List<Message> messages = new ArrayList<>(receivedMessages);
        receivedMessages.clear();
        return messages;
    }

    public void send(Message message){
        assertNotNull(gameController);
        assertFalse(stopped);
        gameController.addMessageToQueue(message, this);
    }

    /**
     * Awaits for a message to be received, while ignoring a set list of status messages.
     * Asserts that the not-ignored message is of the exact expected status.
     *
     * @param expectedStatus the expected status of the awaited message
     * @param ignoredStatus  the list of status to ignore.
     *
     * @return               the received message.
     */
    public Message awaitForMessage(Status expectedStatus, List<Status> ignoredStatus){
        Message message = null;
        boolean empty = true;
        boolean found = false;
        while(!found) {
            //noinspection IdempotentLoopBody
            while (empty) {
                synchronized (this) {
                    empty = receivedMessages.isEmpty();
                }
            }
            synchronized (this) {
                message = receivedMessages.removeFirst();
                found = !ignoredStatus.contains(message.getStatus());
                empty = receivedMessages.isEmpty();
            }
        }
        assertEquals(expectedStatus, message.getStatus());
        return message;
    }

    public Message awaitForMessage(Status expectedStatus){
        return awaitForMessage(expectedStatus, new ArrayList<>());
    }

    public void removeStatus(Status expectedStatus) {
        boolean empty = true;
        boolean found = false;
        int listStart = 0;
        while(!found) {
            //noinspection IdempotentLoopBody
            while (empty) {
                synchronized (this) {
                    empty = receivedMessages.size() - listStart == 0;
                }
            }
            synchronized (this) {
                int messageIndex = receivedMessages.stream().map(Message::getStatus).toList().indexOf(expectedStatus);
                if (messageIndex == -1) {
                    listStart++;
                    continue;
                }
                receivedMessages.remove(messageIndex);
                found = true;
            }
        }
    }

    public boolean containsStatus(Status expectedStatus, int waitDurationMilliseconds){
        try{
            Thread.sleep(waitDurationMilliseconds);
        } catch (InterruptedException e) {
            fail();
        }
        synchronized (this) {
            return receivedMessages.stream().map(Message::getStatus).toList().contains(expectedStatus);
        }
    }

    @Override
    public synchronized void update(Message message){
        if(stopped){
            return;
        }
        if(message.getStatus() == Status.REQUEST_PING){
            if(gameController == null){
                assertNotNull(clientController);
                clientController.addEventToQueue(new LabeledMessage(this, new Message(Status.PING_ACK)));
            } else {
                gameController.receivePing(this);
            }
            return;
        }
        receivedMessages.add(message);
    }

    @Override
    public synchronized void stop(){
        stopped = true;
    }
}