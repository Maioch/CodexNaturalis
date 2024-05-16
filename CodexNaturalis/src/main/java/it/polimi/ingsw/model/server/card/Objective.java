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
 * Class that represents an Objective Card.
 *
 * @author Guglielmo Gatti, Francesco Saverio Nisoli, Marco Maiocchi, Andrea Fidanza
 */
public class Objective implements Serializable {
    private final int objectiveId;
    private final int points;
    private transient Bonus bonus;
    private transient Player owner;

    /**
     * The constructor for the class.
     *
     * @param objectiveId the card's id.
     * @param points the base amount of points awarded by the card.
     */
    public Objective(int objectiveId, int points){
        this.objectiveId = objectiveId;
        this.points = points;
        this.owner = null;
        this.bonus = null;
    }

    /**
     * Copy constructor for the class.
     * @param objective the objective to duplicate.
     */
    public Objective(Objective objective){
        this.objectiveId = objective.objectiveId;
        this.points = objective.points;
        this.owner = null;
        this.bonus = objective.bonus;
    }

    /**
     * Calculates the amount of points gained by satisfying the objective's requirements.
     * @return the amount of points gained.
     */
    public int checkObjective(){
       return this.bonus.calculate(owner);
    }

    /**
     * @return the card's id.
     */
    public int getObjectiveId() { return this.objectiveId; }

    /**
     * @return the card's points.
     */
    public int getPoints() { return this.points; }

    /**
     * @param bonus the card's bonus type.
     */
    public void setBonus(Bonus bonus) { this.bonus = bonus; }

    /**
     * @param owner the player that owns the card, used to obtain the player's board to calculate the multiplier.
     */
    public void setOwner(Player owner) { this.owner = owner; }

    /**
     * Equals method.
     * @param object Object to check.
     * @return true if each field is equals to each field of object.
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
     * Strategy class used to handle the bonuses given out by objective cards.
     *
     * @author Guglielmo Gatti
     */
    public class ContentBonus implements Bonus{
        private final List<Content> sequence;

        /**
         * Constructor for the class.
         * @param sequence list of the bonus's required content
         */
        public ContentBonus(List<Content> sequence){
            this.sequence = sequence;
        }

        /**
         * Calculate the number of instances a pattern of objects occurs on the player's board
         * and multiply it by the card's base points value.
         * @return the amount of points that the card awards on placement.
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
     * Class that represents the objective which consists in having on the board a defined pattern of
     * correctly colored cards; each card can only be used once to calculate this particular bonus.
     *
     * @author Guglielmo Gatti, Francesco Nisoli
     */
    public class PatternBonus implements Bonus{
        private final Map<Point, Content> pattern;

        /**
         * Constructor for the class.
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
         * Find out how many times a specific pattern is present without counting the same card twice and calculate
         * the points awarded to the player.
         * @return the base number of points awarded by the card multiplied by the amount of times the pattern appears.
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