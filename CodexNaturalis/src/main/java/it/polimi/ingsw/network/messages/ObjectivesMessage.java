package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.card.Objective;

import java.util.ArrayList;

/**
 * A message sent by the server, containing all the objectives belonging to the player
 */
public class ObjectivesMessage extends Message {
    private final Objective personalObjective;
    private final ArrayList<Objective> commonObjectives;

    /**
     * Constructor of the class
     * @param personalObjective the player's personal objective
     * @param commonObjectives the common objectives, belonging to all the players
     */
    public ObjectivesMessage(Objective personalObjective, ArrayList<Objective> commonObjectives) {
        super(Status.SEND_OBJECTIVES);
        this.personalObjective = personalObjective;
        this.commonObjectives = commonObjectives;
    }

    /**
     * Getter of the personal objective
     * @return the player's personal objective
     */
    public Objective getPersonalObjective() {
        return personalObjective;
    }

    /**
     * Getter of the common objective
     * @return the game's common objectives, belonging to all the players
     */
    public ArrayList<Objective> getCommonObjectives() {
        return commonObjectives;
    }
}
