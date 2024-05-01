package it.polimi.ingsw.network.messages;

import java.util.ArrayList;

public class ObjectiveScoresMessage extends Message{
    private final ArrayList<Integer> scores;

    /**
     *Constructor for the class
     * @param status the message sent
     * @param value the integer value sent along the message
     */
    public ObjectiveScoresMessage(Status status, ArrayList<Integer> scores){
        super(status);
        this.scores = scores;
    }

    /**
     * Getter method for the integer sent along the message
     * @return value attribute
     */
    public ArrayList<Integer> getScores() {
        return new ArrayList<>(scores);
    }
}
