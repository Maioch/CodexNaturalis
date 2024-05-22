package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message used to handle a show game request.
 */
public class MatchListMessage extends Message {
    private final List<GameInfo> matchList;

    /**
     * Constructor for the class.
     * @param status the status of the message.
     * @param matchList the list of games already created by other clients.
     */
    public MatchListMessage(Status status, List<GameInfo> matchList){
        super(status);
        this.matchList = new ArrayList<>(matchList);
    }

    /**
     * @return a list of the match list (where the integer represents its id, and the string is the nickname
     * of the player who created the match).
     */
    public List<GameInfo> getMatchList(){
        return new ArrayList<>(matchList);
    }
}