package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Message sent along with all the scores used to compute the winner.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class PlayerSummaryMessage extends Message {
    private final Map<Objective, Integer> objectiveScores;
    private final int finalScore;
    private final String playerName;

    /**
     * Constructor for the class.
     *
     * @param objectiveScores a map with each player's objective and the associated score.
     * @param finalScore      the final total score of the player.
     * @param playerName      the player's nickname.
     *
     * @see Objective
     */
    public PlayerSummaryMessage(Map<Objective, Integer> objectiveScores, int finalScore, String playerName){
        super(Status.PLAYER_FINAL_SCORE);
        this.objectiveScores = new LinkedHashMap<>(objectiveScores);
        this.finalScore = finalScore;
        this.playerName = playerName;
    }

    /**
     * Gets the attached final score of the player.
     *
     * @return the final score of the player.
     */
    public int getFinalScore(){
        return finalScore;
    }

    /**
     * Gets the attached objective scores of the player.
     *
     * @return the attached objective scores of the player.
     *
     * @see Objective
     */
    public Map<Objective, Integer> getObjectiveScores(){
        return new LinkedHashMap<>(objectiveScores);
    }

    /**
     * Gets the attached player's nickname.
     *
     * @return attached the player's nickname.
     */
    public String getPlayerName(){ return playerName; }
}