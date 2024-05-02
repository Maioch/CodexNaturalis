package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.HashMap;

/**
 * Client: requests the available matches.
 * Server: in response, will in the next message, send the available matches.
 */
public class MatchListMessage extends Message {
    private final HashMap<Integer,String> matchList;

    /**
     * Constructor for the class
     * @param matchList the list of games already created by other clients
     */
    public MatchListMessage(HashMap<Integer,String> matchList){
        super(Status.SHOW_MATCHES);
        this.matchList = matchList;
    }

    /**
     * Getter method of the match list
     * @return hashmap of the match list (where the integer represents its id, and the string is the nickname of the player who created the match)
     */
    public HashMap<Integer,String> getMatchList(){
        return matchList;
    }
}
