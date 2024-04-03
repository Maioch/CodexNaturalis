package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.*;
import java.util.ArrayList;

/**
 * A class that represents a gold card
 * Used to extend (and so, specify) BasicCard
 * @author Francesco Saverio Nisoli
 */

public class GoldCard extends BasicCard {
    private final ArrayList<Content> requirements;
    private final Bonus bonus;
    private final Player owner;

    
    /**
     * @param cardTemplate it's a "basic" card previously initialized, which serves as a value reference for the GoldCard instantiated
     * @param requirements the resources needed in order to play the card
     * @param bonus the bonus object which the card possesses
     * @param owner the player who owns the card
    */
    public GoldCard(BasicCard cardTemplate, ArrayList<Content> requirements, Bonus bonus, Player owner){
        super(cardTemplate.cardId, cardTemplate.color, cardTemplate.corners, cardTemplate.points, cardTemplate.resources);
        this.requirements = new ArrayList<>(requirements);
        this.bonus = bonus;
        this.owner = owner;
    }

    /**
     * Getter of the "requirements" parameter
     * @return the requirements needed to play the card
     */
    @Override
    public ArrayList<Content> getRequirements(){
        return new ArrayList<>(this.requirements);
    }

    /**
     * A method that calculates the total points value that the card gives when played (by the owner of it)
     * @return points value gained by playing the card
     */
    @Override
    public int getPoints(){
        return bonus.calculate();
    }

    /**
     * Class that calculates the bonus given by a gold card that gives points per corner covered by the card itself
     *
     * @author Andera Fidanza
     */
    public class CornerBonus implements Bonus{

        /**
         * Calculates the total points
         * @return total points
         */
        @Override
        public int calculate(){
            int bonusPoints = points;

            for(BasicCard card : owner.getPlacedCards())
                for(Location loc : card.getAllCorners().keySet())
                    if(corners.get(loc).getVisibility() && card.getAllCorners().get(loc).isSamePosition(corners.get(loc)))
                        bonusPoints += points;

            return bonusPoints;
        }
    }

    /**
     * Class that calculates the bonus given by a gold card that gives points per visible object in the board of the
     * player, including card itself
     *
     * @author Marco Maiocchi
     */
    public class ObjectBonus implements Bonus{
        private final Content object;

        /**
         * Constructor for the class
         * @param object object used for the points multiplier
         */
        public ObjectBonus(Content object){
            this.object = object;
        }

        /**
         * Method that calculates the points given to the player by the gold card he plays
         * @return points given to the player
         */
        @Override
        public int calculate(){
            return points * owner.getPlayerContent().get(object);
        }
    }
}