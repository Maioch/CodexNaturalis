package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message sent along with all the scores used to compute the winner.
 */
public class PlayerSummaryMessage extends Message {
    private final Map<Objective, Integer> objectiveScores;
    private final int finalScore;
    private final String playerName;

    /**
     * Constructor for the class.
     * @param objectiveScores a map with each player's objective and the associated score.
     * @param finalScore the final total score of the player.
     */
    public PlayerSummaryMessage(Map<Objective, Integer> objectiveScores, int finalScore, String playerName){
        super(Status.PLAYER_FINAL_SCORE);
        this.objectiveScores = new LinkedHashMap<>(objectiveScores);
        this.finalScore = finalScore;
        this.playerName = playerName;
    }

    /**
     * @return the final score of a player.
     */
    public int getFinalScore(){
        return finalScore;
    }

    /**
     * @return the objective scores of a player.
     */
    public Map<Objective, Integer> getObjectiveScores(){
        return new LinkedHashMap<>(objectiveScores);
    }

    public String getPlayerName(){ return playerName; }
}