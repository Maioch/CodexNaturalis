package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.HashMap;

/**
 * Message sent at the end of the game to inform the players about their objectives results and final score
 */
public class PlayerSummaryMessage extends Message {
    private final HashMap<Objective, Integer> objectiveScores;
    private final int finalScore;

    /**
     * Constructor for the class
     * @param objectiveScores a map with each player's objective and the associated score
     * @param finalScore the final total score of the player
     */
    public PlayerSummaryMessage(HashMap<Objective, Integer> objectiveScores, int finalScore){
        super(Status.PLAYER_FINAL_SCORE);
        this.objectiveScores = objectiveScores;
        this.finalScore = finalScore;
    }

    /**
     * @return the finalScore attribute
     */
    public int getFinalScore(){
        return finalScore;
    }

    /**
     * @return the objectiveScores attribute
     */
    public HashMap<Objective, Integer> getObjectiveScores(){
        return new HashMap<>(objectiveScores);
    }
}