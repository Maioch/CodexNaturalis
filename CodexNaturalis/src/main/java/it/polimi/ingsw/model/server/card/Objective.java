package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.Player;

import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Objective represents the cards in the game that are the player's target during a game, that, when fulfilled, award
 * a large amount of points.
 */
public class Objective implements Serializable {

    private final int objectiveId;
    private final int points;
    private transient Bonus bonus;
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
     * Returns the ID of the card
     *
     * @return the card's ID.
     */
    public int getObjectiveId() { return this.objectiveId; }

    /**
     * Returns the objectives' base points.
     *
     * @return the card's points.
     */
    public int getPoints() { return this.points; }

    /**
     * Sets the bonus type associated to this objective.
     *
     * @param bonus the card's bonus type.
     *
     * @see ContentBonus
     * @see PatternBonus
     */
    public void setBonus(Bonus bonus) { this.bonus = bonus; }

    /**
     * Sets the player that owns the objective, used to obtain the player's board to calculate the multiplier.
     *
     * @param owner the card's owner.
     */
    public void setOwner(Player owner) { this.owner = owner; }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object objective checked.
     *
     * @return       true if this objective is equal to the parameter one.
     */
    @Override
    public boolean equals(Object object) {
        if(object.getClass() != this.getClass()){
            return false;
        }
        Objective objective = (Objective) object;
        return objective.objectiveId == this.objectiveId;
    }

    /**
     * ContentBonus implements a method that calculates the total amount of points given by an objective that gives
     * them per content sequences present in the player's board.
     */
    public class ContentBonus implements Bonus{
        private final List<Content> sequence;

        /**
         * Class constructor.
         *
         * @param sequence list of the bonus' required content.
         */
        public ContentBonus(List<Content> sequence){
            this.sequence = sequence;
        }

        /**
         * Returns the total amount of points given to the player when he satisfies the objective.
         *
         * @return the card's awarded points.
         */
        @Override
        public int calculate(Player cardOwner){
            Map<Content, Integer> timesFound = cardOwner.getPlayerContent();
            for(Content content : Content.values()){
                if(sequence.contains(content)){
                    int timesFoundInSequence = sequence.stream()
                            .filter(x -> x == content)
                            .mapToInt(x -> 1)
                            .reduce(0,Integer::sum);
                    timesFound.put(content, timesFound.get(content) / timesFoundInSequence);
                }
                else{
                    timesFound.remove(content);
                }
            }
            return points * timesFound.values().stream().min(Integer::compareTo).orElse(0);
        }
    }

    /**
     * ContentBonus implements a method that calculates the total amount of points given by an objective that gives
     * them per defined pattern occurrence of correctly colored cards; each card can only be used once to calculate
     * this particular bonus.
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
         * Returns the total amount of points given to the player when he satisfies the objective.
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
                    Corner cardBLCorner = card.getAllCorners().stream()
                            .filter(c -> c.getLocation() == Location.BL)
                            .findAny().orElseThrow();
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