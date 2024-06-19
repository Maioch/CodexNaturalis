package it.polimi.ingsw.network.shared.messages.setup;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

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