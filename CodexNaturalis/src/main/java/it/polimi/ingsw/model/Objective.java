package it.polimi.ingsw.model;

/**
 * Class that represents an Objective Card.
 * @author Guglielmo Gatti
 */
public class Objective {
    private final int objectiveId;
    private final int points;
    private final Bonus bonus;
    private final Player owner;

    /**
     * Calculates the amount of points gained by satisfying the objective's requirements.
     * @return the amount of points gained
     */
    public int checkObjective(){
        return bonus.calculate(points);
    }

    /**
     * @param objectiveId the card's id
     * @param points the base amount of points awarded by the card
     * @param bonus the bonus object, used to calculate the multiplier
     * @param owner the player that owns the card, used to obtain the player's board to calculate the multiplier
     */
    Objective(int objectiveId, int points, Bonus bonus, Player owner){
        this.objectiveId = objectiveId;
        this.points = points;
        this.bonus = bonus;
        this.owner = owner;
    }
}
