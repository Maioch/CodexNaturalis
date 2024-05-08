package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * A message sent by the server, containing all the objectives belonging to the player
 */
public class ObjectivesMessage extends Message {
    private final List<Objective> personalObjectives;
    private final List<Objective> commonObjectives;

    /**
     * Constructor of the class
     * @param personalObjectives the player's personal objective
     * @param commonObjectives the common objectives, belonging to all the players
     */
    public ObjectivesMessage(Status status, List<Objective> personalObjectives, List<Objective> commonObjectives){
        super(status);
        this.personalObjectives = new ArrayList<>(personalObjectives);
        this.commonObjectives = new ArrayList<>(commonObjectives);
    }

    /**
     * @return the player's personal objective
     */
    public List<Objective> getPersonalObjectives(){
        return new ArrayList<Objective>(personalObjectives);
    }

    /**
     * @return the game's common objectives, belonging to all the players
     */
    public List<Objective> getCommonObjectives() {
        return new ArrayList<Objective>(commonObjectives);
    }
}
