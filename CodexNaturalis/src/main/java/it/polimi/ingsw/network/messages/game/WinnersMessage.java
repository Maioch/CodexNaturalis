package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message that contains the winner/s of a game.
 */
public class WinnersMessage extends Message {
    private final List<String> winners;

    /**
     * Constructor for the class.
     * @param winners a list of the winners name.
     */
    public WinnersMessage(List<String> winners) {
        super(Status.DECLARE_WINNER);
        this.winners = winners;
    }

    /**
     * @return a list containing all the winners nicknames.
     */
    public List<String> getWinners() {
        return new ArrayList<>(winners);
    }
}
