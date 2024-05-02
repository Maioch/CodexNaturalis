package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;

public class ObjectiveScoresMessage extends Message {
    private final ArrayList<Integer> scores;

    /**
     *Constructor for the class
     * @param status the message sent
     * @param scores the integer, other players', score values
     */
    public ObjectiveScoresMessage(Status status, ArrayList<Integer> scores){
        super(status);
        this.scores = scores;
    }

    /**
     * Getter method for the scores sent along the message
     * @return value attribute
     */
    public ArrayList<Integer> getScores() {
        return new ArrayList<>(scores);
    }
}
