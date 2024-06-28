package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message used to show each objective in a game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ObjectivesMessage extends Message {
    private final List<Objective> objectives;

    /**
     * Constructor for the class.
     *
     * @param status     the message status.
     * @param objectives the objectives.
     *
     * @see Status
     * @see Objective
     */
    public ObjectivesMessage(Status status, List<Objective> objectives){
        super(status);
        this.objectives = new ArrayList<>(objectives);
    }

    /**
     * Gets the attached objectives.
     *
     * @return the attached objectives.
     *
     * @see Objective
     */
    public List<Objective> getObjectives(){
        return new ArrayList<>(objectives);
    }
}