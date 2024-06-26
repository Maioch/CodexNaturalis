package it.polimi.ingsw.network.shared.messages.setup;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message used to handle a show game request.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class MatchListMessage extends Message {
    private final List<GameInfo> matchList;

    /**
     * Constructor for the class.
     *
     * @param status    the status of the message.
     * @param matchList the list of games already created by other clients.
     *
     * @see Status
     * @see GameInfo
     */
    public MatchListMessage(Status status, List<GameInfo> matchList){
        super(status);
        this.matchList = new ArrayList<>(matchList);
    }

    /**
     * Gets the attached match list.
     *
     * @return the attached match list.
     *
     * @see GameInfo
     */
    public List<GameInfo> getMatchList(){
        return new ArrayList<>(matchList);
    }
}