package it.polimi.ingsw.model;

import it.polimi.ingsw.model.card.BasicCard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class that represents an Objective Card.
 *
 * @author Guglielmo Gatti, Francesco Saverio Nisoli
 */
public class Objective {
    private final int objectiveId;
    private final int points;
    private Bonus bonus;
    private Player owner;

    /**
     * @param objectiveId the card's id
     * @param points the base amount of points awarded by the card
     */
    public Objective(int objectiveId, int points){
        this.objectiveId = objectiveId;
        this.points = points;
        this.owner = null;
        this.bonus = null;
    }

    /**
     * Calculates the amount of points gained by satisfying the objective's requirements.
     * @return the amount of points gained
     */
    public int checkObjective(){
        return this.bonus.calculate();
    }

    /**
     * Getter for objective id
     * @return the id
     */
    public int getObjectiveId() { return this.objectiveId; }

    /**
     * Getter for points
     * @return the points
     */
    public int getPoints() { return this.points; }

    /**
     * Setter for bonus
     * @param bonus the bonus
     */
    public void setBonus(Bonus bonus) { this.bonus = bonus; }

    /**
     * A setter method of the "owner" attribute
     * @param owner the player that owns the card, used to obtain the player's board to calculate the multiplier
     */
    public void setOwner(Player owner) { this.owner = owner; }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object) {
        if(object.getClass() != this.getClass()){
            return false;
        }
        Objective objective = (Objective) object;
        boolean isSameBonus = (this.bonus == null) ?
                objective.bonus == null :
                objective.bonus != null && this.bonus.equals(objective.bonus);
        return objective.objectiveId == this.objectiveId &&
                objective.points == this.points &&
                isSameBonus;
    }

    /**
     * Strategy class used to handle the bonuses given out by objective cards
     * @author Guglielmo Gatti
     */
    public class ContentBonus implements Bonus{
        private final ArrayList<Content> sequence;

        public ContentBonus(ArrayList<Content> sequence){
            this.sequence = sequence;
        }

        /**
         * calculate the number of instances a pattern of objects occurs on the player's board
         * and multiply it by the card's base points value
         * @return the amount of points that the card should award on placement
         */
        @Override
        public int calculate(){
            HashMap<Content, Integer> timesFound = owner.getPlayerContent();
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

        /**
         * Equals method.
         * @param object Object to check
         * @return true if each field is equals to each field of object
         */
        @Override
        public boolean equals(Object object) {
            if(object.getClass() != this.getClass()){
                return false;
            }
            ContentBonus objective = (ContentBonus) object;
            return objective.sequence.equals(this.sequence);
        }

    }

    /**
     * Class implementing pattern bonuses (of objective cards)
     */
    public class PatternBonus implements Bonus{
        private final HashMap<Point, Content> pattern;

        public PatternBonus(HashMap<Point, Content> pattern){
            boolean isValidPattern = pattern.values().stream()
                    .filter(x -> x.isObject() || x == Content.EMPTY || x == Content.WHITE)
                    .findAny()
                    .isEmpty();
            if(!isValidPattern){
                throw new RuntimeException(
                        String.format("Invalid pattern content in card %d", objectiveId));
            }
            this.pattern = pattern;
        }


        /**
         * A method that searches for a certain pattern in the owner's placed cards
         * @return the number of points calculated
         */
        @Override
        public int calculate(){
            record patternReference(int x, int y, Content color){}
            int calculatedPoints;
            boolean calculationCompleted;
            int XOffSet, YOffSet;
            ArrayList<patternReference> patternReferences;
            ArrayList<BasicCard> placedCards;
            ArrayList<Integer> markedIndexesPlacedCards, markedIndexesPattern;

            calculatedPoints = 0;
            XOffSet = 0;
            YOffSet = 0;
            patternReferences = new ArrayList<>();
            placedCards = owner.getPlacedCards();
            markedIndexesPlacedCards = new ArrayList<>(); //list of analysed indexes of the placed cards
            markedIndexesPattern = new ArrayList<>(); //list of analysed indexes of the pattern elements

            pattern.forEach((point, content) -> patternReferences.add(new patternReference(point.x, point.y, content)));

            do{
                calculationCompleted = true;

                for(int i = 0; i < placedCards.size(); i++){    //streaming the placed cards array
                    for(int k = 0; k < patternReferences.size(); k++){  // streaming of the patternReferences array, in order to eventually find matches with the cards

                        if (placedCards.get(i).getColor() == patternReferences.get(k).color) { //color match between a pattern element and a card
                            if(markedIndexesPattern.isEmpty()){     //first pattern element to be matched
                                markedIndexesPattern.add(k);    //saving the index of the pattern element matched
                                markedIndexesPlacedCards.add(i);    //saving the index of the matched placed card
                                XOffSet = placedCards.get(i).getAllCorners().get(Location.BL).getX() - patternReferences.get(k).x;  //the first match, sets the offset considered during the search of matches
                                YOffSet = placedCards.get(i).getAllCorners().get(Location.BL).getY() - patternReferences.get(k).y;

                            }else {
                                for (int indexValuePattern : markedIndexesPattern) {
                                    for(int indexValueCard : markedIndexesPlacedCards) {
                                        if (indexValuePattern != k && indexValueCard != i &&    //condition that makes sure there's no doubling of the elements considered
                                                placedCards.get(i).getAllCorners().get(Location.BL).getX() - XOffSet == patternReferences.get(k).x &&   //condition that makes sure the x coordinates are coherent within the pattern constrains
                                                placedCards.get(i).getAllCorners().get(Location.BL).getY() - YOffSet == patternReferences.get(k).y)     //condition that makes sure the y coordinates are coherent within the pattern constrains
                                        {
                                            markedIndexesPlacedCards.add(i);
                                            markedIndexesPattern.add(k);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(patternReferences.size() == markedIndexesPattern.size()){    //pattern found, we remove cards from it
                        for (int indexValueCard : markedIndexesPlacedCards) {
                            placedCards.remove(indexValueCard);
                        }
                        markedIndexesPattern.clear();
                        markedIndexesPlacedCards.clear();
                        calculatedPoints = calculatedPoints + points;
                        calculationCompleted = false;
                    }
                    //~rough solution~ the first found pattern match, could be not part of a pattern, causing the process to end with "calculationCompleted" == true, while it shouldn't.
                    if(i == placedCards.size() && calculationCompleted){
                        i = markedIndexesPlacedCards.get(0);    //the index returns to the "wrong" match position, letting a new search that isn't based on that match.
                        markedIndexesPattern.clear();               //matches cleared, new search next
                        markedIndexesPlacedCards.clear();
                    }
                }

            }while(!calculationCompleted);

            return calculatedPoints;
        }

        /**
         * Equals method.
         * @param object Object to check
         * @return true if each field is equals to each field of object
         */
        @Override
        public boolean equals(Object object) {
            if(object.getClass() != this.getClass()){
                return false;
            }
            PatternBonus objective = (PatternBonus) object;
            return objective.pattern.equals(this.pattern);
        }
    }

    /**
     * an attempt to make the PatternBonus class shorter and more legible.
     * @author Guglielmo Gatti, Francesco Nisoli
     */
    public class AlternativePatternBonus implements Bonus{
        private final HashMap<Point, Content> pattern;

        /**
         * @param pattern hashmap describing the required pattern by pairing each color
         *                to its relative coordinates
         */
        public AlternativePatternBonus(HashMap<Point, Content> pattern){
            boolean isValidPattern = pattern.values().stream()
                    .filter(x -> x.isObject() || x == Content.EMPTY || x == Content.WHITE)
                    .findAny()
                    .isEmpty();
            if(!isValidPattern){
                throw new RuntimeException(
                        String.format("Invalid pattern content in card %d", objectiveId));
            }
            this.pattern = pattern;
        }

        /**
         * find out how many times a specific pattern is present without counting the same card twice and calculate
         * the points awarded to the player.
         * @return the base number of points awarded by the card multiplied by the amount of times the pattern appears
         */
        @Override
        public int calculate(){
            //Generate a color hashmap from the player's board
            HashMap<Point,Content> colorHashMap = new HashMap<>(){{
                for(BasicCard card : owner.getPlacedCards()){
                    Corner cardBLCorner = card.getAllCorners().get(Location.BL);
                    Point cardPosition = new Point(cardBLCorner.getX(), cardBLCorner.getY());
                    put(cardPosition, card.getColor());
                }
            }};
            //Check how many times the pattern is present (without counting any card twice)
            int timesAppeared = 0;
            for(Map.Entry<Point,Content> entry : colorHashMap.entrySet()){
                Point baseOffset = new Point(entry.getKey());
                Point offset = new Point(baseOffset);
                ArrayList<Point> evaluatedPoints = new ArrayList<>();
                //Sub-iteration used to check for the pattern itself
                boolean patternFound = true;
                for(Map.Entry<Point, Content> patternEntry : pattern.entrySet()){
                    evaluatedPoints.add(entry.getKey());
                    offset.move( baseOffset.x + patternEntry.getKey().x, baseOffset.y + patternEntry.getKey().y);
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
            return points * timesAppeared;
        }

        /**
         * Equals method.
         * @param object Object to check
         * @return true if each field is equals to each field of object
         */
        @Override
        public boolean equals(Object object) {
            if(object.getClass() != this.getClass()){
                return false;
            }
            AlternativePatternBonus objective = (AlternativePatternBonus) object;
            return objective.pattern.equals(this.pattern);
        }
    }
}