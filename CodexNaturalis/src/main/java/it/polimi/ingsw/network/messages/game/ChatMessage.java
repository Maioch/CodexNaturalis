package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * A message used when the client wants to chat
 */
public class ChatMessage extends Message{
    private final String message;
    private final String sender;
    private final List<String> recipients; //might be null

    /**
     * Constructor of the chat message
     * @param message the string content
     * @param sender the sender's (nick)name
     * @param recipients the recipients' (nick)names
     */
    public ChatMessage(String message, String sender, List<String> recipients){
        super(Status.CHAT);
        this.message = message;
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
    }

    /**
     * @return the message attribute
     */
    public String getMessage(){
        return message;
    }

    /**
     * @return the sender attribute
     */
    public String getSender(){
        return sender;
    }

    /**
     * @return the recipients attribute, if present
     */
    public List<String> getRecipients(){
        return new ArrayList<>(recipients);
    }
}