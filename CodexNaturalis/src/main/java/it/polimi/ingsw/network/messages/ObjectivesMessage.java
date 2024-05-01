package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.model.server.card.Objective;

import java.util.ArrayList;

/**
 * A message sent by the server, containing all the objectives belonging to the player
 */
public class ObjectivesMessage extends Message {
    private final ArrayList<Objective> personalObjectives;
    private final ArrayList<Objective> commonObjectives;

    /**
     * Constructor of the class
     * @param personalObjectives the player's personal objective
     * @param commonObjectives the common objectives, belonging to all the players
     */
    public ObjectivesMessage(Status status,ArrayList<Objective> personalObjectives, ArrayList<Objective> commonObjectives) {
        super(status);
        this.personalObjectives = personalObjectives;
        this.commonObjectives = commonObjectives;
    }

    /**
     * Getter of the personal objective
     * @return the player's personal objective
     */
    public ArrayList<Objective> getPersonalObjective() {
        return new ArrayList<Objective>(personalObjectives);
    }

    /**
     * Getter of the common objective
     * @return the game's common objectives, belonging to all the players
     */
    public ArrayList<Objective> getCommonObjectives() {
        return new ArrayList<Objective>(commonObjectives);
    }
}
