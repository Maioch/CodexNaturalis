package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

public class WinnersMessage extends Message {
    private final List<String> winners;

    /**
     * Constructor for the class
     * @param winners list of the wnners' name
     */
    public WinnersMessage(List<String> winners) {
        super(Status.DECLARE_WINNER);
        this.winners = winners;
    }

    /**
     * Getter method for the string sent along the message
     * @return string attribute
     */
    public List<String> getWinners() {
        return new ArrayList<String>(winners);
    }
}
