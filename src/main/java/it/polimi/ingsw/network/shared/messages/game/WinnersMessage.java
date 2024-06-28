package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message that contains the winner/s of a game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class WinnersMessage extends Message {
    private final List<String> winners;

    /**
     * Constructor for the class.
     *
     * @param winners a list of the winners name.
     */
    public WinnersMessage(List<String> winners) {
        super(Status.DECLARE_WINNER);
        this.winners = winners;
    }

    /**
     * Gets the attached list of the winners nickname.
     *
     * @return the attached list of the winners nicknames.
     */
    public List<String> getWinners() {
        return new ArrayList<>(winners);
    }
}