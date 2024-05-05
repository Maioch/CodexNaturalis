package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;

/**
 * A message used when the client wants to chat
 */
public class ChatMessage extends Message{
    private final String message;
    private final String sender;
    private final ArrayList<String> recipients; //might be null

    /**
     * Constructor of the chat message
     * @param message the string content
     * @param sender the sender's (nick)name
     * @param recipients the recipients' (nick)names
     */
    public ChatMessage(String message, String sender, ArrayList<String> recipients){
        super(Status.CHAT);
        this.message = message;
        this.sender = sender;
        this.recipients = recipients;
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
    public ArrayList<String> getRecipients(){
        return new ArrayList<>(recipients);
    }
}