package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message used for a chat usage request.
 */
public class ChatMessage extends Message{
    private final String message;
    private final String sender;
    private final List<String> recipients; //might be null

    /**
     * Constructor for the class.
     * @param message the string content.
     * @param sender the sender's (nick)name.
     * @param recipients the recipients' (nick)names.
     */
    public ChatMessage(String message, String sender, List<String> recipients){
        super(Status.CHAT);
        this.message = message;
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
    }

    /**
     * @return the chat message.
     */
    public String getMessage(){
        return message;
    }

    /**
     * @return the message sender.
     */
    public String getSender(){
        return sender;
    }

    /**
     * @return the recipients attribute, if present.
     */
    public List<String> getRecipients(){
        return new ArrayList<>(recipients);
    }
}