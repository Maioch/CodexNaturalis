package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message used to show each objective in a game.
 */
public class ObjectivesMessage extends Message {
    private final List<Objective> objectives;

    /**
     * Constructor for the class.
     * @param status the message status.
     * @param objectives the objectives.
     */
    public ObjectivesMessage(Status status, List<Objective> objectives){
        super(status);
        this.objectives = new ArrayList<>(objectives);
    }

    /**
     * @return the objectives.
     */
    public List<Objective> getObjectives(){
        return new ArrayList<>(objectives);
    }
}