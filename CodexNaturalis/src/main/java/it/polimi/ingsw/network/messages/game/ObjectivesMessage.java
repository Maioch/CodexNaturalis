package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.Objective;
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
     * @param personalObjectives the player's personal objective.
     * @param commonObjectives the common objectives, belonging to all the players.
     */
    public ObjectivesMessage(Status status, List<Objective> objectives){
        super(status);
        this.objectives = new ArrayList<>(objectives);
    }

    /**
     * @return the player's personal objective.
     */
    public List<Objective> getObjectives(){
        return new ArrayList<>(objectives);
    }
}