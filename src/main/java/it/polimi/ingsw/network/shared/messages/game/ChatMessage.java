package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message used for a chat usage request.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ChatMessage extends Message{
    private final String message;
    private final String sender;
    private final List<String> recipients; //might be null

    /**
     * Constructor for the class.
     *
     * @param message    the string content.
     * @param sender     the sender's (nick)name.
     * @param recipients the recipients' (nick)names.
     */
    public ChatMessage(String message, String sender, List<String> recipients){
        super(Status.CHAT);
        this.message = message;
        this.sender = sender;
        this.recipients = new ArrayList<>(recipients);
    }

    /**
     * Gets the attached chat message.
     *
     * @return the attached chat message.
     */
    public String getMessage(){
        return message;
    }

    /**
     * Gets the attached message's sender.
     *
     * @return the attached message's sender.
     */
    public String getSender(){
        return sender;
    }

    /**
     * Gets the attached recipients attribute, if present.
     *
     * @return the attached recipients attribute, if present.
     */
    public List<String> getRecipients(){
        return new ArrayList<>(recipients);
    }
}