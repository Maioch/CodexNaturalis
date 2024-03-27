package it.polimi.ingsw.model;

import it.polimi.ingsw.model.card.BasicCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Class that represents an Objective Card.
 * @author Guglielmo Gatti, Francesco Saverio Nisoli
 */
public class Objective {
    private final int objectiveId;
    private final int points;
    private final Bonus bonus;
    private final Player owner;

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

    /**
     * Calculates the amount of points gained by satisfying the objective's requirements.
     * @return the amount of points gained
     */
    public int checkObjective(){
        return this.bonus.calculate();
    }

    public int getObjectiveId() { return this.objectiveId; }

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
    }

    /**
     * Class implementing pattern bonuses (of objective cards)
     */
    public class PatternBonus implements Bonus{
        private final ArrayList<ArrayList<Content>> pattern;

        public PatternBonus(ArrayList<ArrayList<Content>> pattern){
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
            patternReferences = new ArrayList<patternReference>();
            placedCards = owner.getPlacedCards();
            markedIndexesPlacedCards = new ArrayList<Integer>(); //list of analysed indexes of the placed cards
            markedIndexesPattern = new ArrayList<Integer>(); //list of analysed indexes of the pattern elements

            //part of the code that saves in a record the clue parts of the pattern (coordinates, color)
            for(int i = 0; i < pattern.size(); i++){
                for(int j = 0; j < pattern.get(i).size(); j++){
                    if(pattern.get(i).get(j) != Content.WHITE){
                        patternReferences.add(new patternReference(i, j, pattern.get(i).get(j)));
                    }
                }
            }

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
                }

            }while(!calculationCompleted);

            return calculatedPoints;
        }
    }
}