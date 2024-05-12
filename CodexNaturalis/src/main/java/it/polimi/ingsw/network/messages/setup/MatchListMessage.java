package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.HashMap;
import java.util.Map;

/**
 * Message used to handle a show game request.
 */
public class MatchListMessage extends Message {
    private final Map<Integer,String> matchList;

    /**
     * Constructor for the class.
     * @param status the status of the message.
     * @param matchList the list of games already created by other clients.
     */
    public MatchListMessage(Status status, Map<Integer,String> matchList){
        super(status);
        this.matchList = new HashMap<>(matchList);
    }

    /**
     * @return a map of the match list (where the integer represents its id, and the string is the nickname
     * of the player who created the match).
     */
    public Map<Integer,String> getMatchList(){
        return matchList;
    }
}
