package it.polimi.ingsw.model.shared.card;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;

import java.awt.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the objective cards of the game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Objective implements Serializable {

    //the objective's id.
    private final int objectiveId;

    //the points awarded by fulfilling the objective once.
    private final int points;

    //the bonus that yields the multiplier with which the total awarded points are going to be calculated.
    private transient Bonus bonus;

    //the player who owns the objective
    private transient Player owner;

    /**
     * Class constructor.
     *
     * @param objectiveId the card's id.
     * @param points      the base amount of points awarded by the card.
     */
    public Objective(int objectiveId, int points){
        this.objectiveId = objectiveId;
        this.points = points;
        this.owner = null;
        this.bonus = null;
    }

    /**
     * Class copy-constructor.
     *
     * @param objective the instance to copy.
     */
    public Objective(Objective objective){
        this.objectiveId = objective.objectiveId;
        this.points = objective.points;
        this.owner = null;
        this.bonus = objective.bonus;
    }

    /**
     * Calculates the amount of points gained by satisfying the objective's requirements.
     *
     * @return the amount of points awarded.
     */
    public int checkObjective(){
       return this.bonus.calculate(owner);
    }

    /**
     * Gets the ID of the card
     *
     * @return the card's ID.
     */
    public int getObjectiveId() {
        return this.objectiveId;
    }

    /**
     * Gets the objectives' base points.
     *
     * @return the card's points.
     */
    public int getPoints() {
        return this.points;
    }

    /**
     * Sets the bonus type associated to this objective.
     *
     * @param bonus the card's bonus type.
     *
     * @see Bonus
     * @see ContentBonus
     * @see PatternBonus
     */
    public void setBonus(Bonus bonus) { this.bonus = bonus; }

    /**
     * Sets the player that owns the objective.
     *
     * @param owner the card's owner.
     *
     * @see Player
     */
    public void setOwner(Player owner) {
        this.owner = owner;
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object objective checked.
     *
     * @return       true if this objective is equal to the parameter one.
     */
    @Override
    public boolean equals(Object object) {
        if(object instanceof Objective objective) {
            return objective.objectiveId == this.objectiveId;
        }
        return false;
    }

    /**
     * Implements calculate using the amount of specified objects found in the objective owner's board
     * as the multiplier that gets applied to the objective's base points.
     */
    public class ContentBonus implements Bonus{
        private final List<Content> sequence;

        /**
         * Class constructor.
         *
         * @param sequence list of the bonus' required content.
         */
        public ContentBonus(List<Content> sequence){
            boolean isValidPattern = sequence.stream()
                    .filter(x -> x.isEmpty() || x == Content.WHITE)
                    .findAny()
                    .isEmpty();
            if(!isValidPattern){
                throw new RuntimeException(
                        String.format("Invalid content in card %d", objectiveId));
            }
            this.sequence = sequence;
        }

        /**
         * Gets the total amount of points given to the player when he satisfies the objective.
         *
         * @return the card's awarded points.
         */
        @Override
        public int calculate(Player cardOwner){
            Map<Content, Integer> timesFound = cardOwner.getPlayerContent();
            for(Content content : Content.values()){
                int timesFoundInSequence = sequence.stream()
                        .filter(x -> x == content)
                        .mapToInt(x -> 1)
                        .sum();
                if(timesFoundInSequence != 0) {
                    timesFound.put(content, timesFound.get(content) / timesFoundInSequence);
                } else {
                    timesFound.remove(content);
                }
            }
            return points * timesFound.values().stream().min(Integer::compareTo).orElse(0);
        }
    }

    /**
     * Implements calculate by counting the specified pattern occurrences on the objective owner's board;
     * each card can only be used once to calculate this particular bonus.
     */
    public class PatternBonus implements Bonus{
        private final Map<Point, Content> pattern;

        /**
         * Class constructor.
         *
         * @param pattern hashmap describing the required pattern by pairing each color to its relative coordinates.
         */
        public PatternBonus(Map<Point, Content> pattern){
            boolean isValidPattern = pattern.values().stream()
                    .filter(x -> x.isObject() || x.isEmpty() || x == Content.WHITE)
                    .findAny()
                    .isEmpty();
            if(!isValidPattern){
                throw new RuntimeException(
                        String.format("Invalid pattern content in card %d", objectiveId));
            }
            this.pattern = pattern;
        }

        /**
         * Gets the total amount of points given to the player when he satisfies the objective.
         *
         * @return the card's awarded points.
         */
        @Override
        public int calculate(Player cardOwner){
            Point min = new Point(0,0);
            Point max = new Point(0,0);
            //Generate a color hashmap from the player's board
            Map<Point,Content> colorHashMap = new HashMap<>(){{
                for(BasicCard card : cardOwner.getPlacedCards()){
                    Corner cardBLCorner = card.getCorner(Location.BL);
                    Point cardPosition = new Point(cardBLCorner.getX(), cardBLCorner.getY());
                    min.x = Math.min(cardBLCorner.getX(), min.x);
                    min.y = Math.min(cardBLCorner.getY(), min.y);
                    max.x = Math.max(cardBLCorner.getX(), max.x);
                    max.y = Math.max(cardBLCorner.getY(), max.y);
                    put(cardPosition, card.getColor());
                }
            }};
            //Check how many times the pattern is present (without counting any card twice)
            //The checking order is now guaranteed in order to avoid evaluation issues with chained patterns:
            //the search starts from the bottom left and ends at the top right.
            int timesAppeared = 0;
            Point baseOffset = new Point();
            for(int y = min.y; y <= max.y; y++){
                for(int x = min.x; x <= max.x; x++){
                    baseOffset.move(x,y);
                    Point offset = new Point(baseOffset);
                    List<Point> evaluatedPoints = new ArrayList<>();
                    //Sub-iteration used to check for the pattern itself
                    boolean patternFound = true;
                    for(Map.Entry<Point, Content> patternEntry : pattern.entrySet()){
                        offset.move( baseOffset.x + patternEntry.getKey().x,
                                baseOffset.y + patternEntry.getKey().y);
                        evaluatedPoints.add(new Point(offset));
                        if(colorHashMap.get(offset) != patternEntry.getValue()){
                            patternFound = false;
                            break;
                        }
                    }
                    //Mark all the cards that have been already counted as part of a pattern as WHITE
                    if(patternFound){
                        for(Point point : evaluatedPoints){
                            colorHashMap.put(point,Content.WHITE);
                        }
                    }
                    timesAppeared += patternFound ? 1 : 0;
                }
            }
            return points * timesAppeared;
        }
    }
}