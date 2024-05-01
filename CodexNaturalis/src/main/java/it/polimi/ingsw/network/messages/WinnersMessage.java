package it.polimi.ingsw.network.messages;

import java.util.ArrayList;
import java.util.List;

public class WinnersMessage extends Message{
    private final List<String> winners;

    /**
     * Constructor for the class
     * @param status the message sent
     * @param string the string sent along the message
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
