package it.polimi.ingsw.model.shared.card;

import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents one of the types of cards in the game that need to be treated in a special manner: a gold card can only be
 * placed if the player has on his board a specific amount of visible resources; additionally, the points awarded for their
 * placement can be both a set amount (just like some resource cards), or they can be calculated based on the number of
 * symbols of a given type present on the player's board.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see BasicCard
 */

public class GoldCard extends BasicCard {

    //the resources needed to place the card
    private final List<Content> requirements;

    //the bonus that alters the amount of points given when placing the card, if present.
    private transient Bonus bonus;
    
    /**
     * Class constructor.
     *
     * @param cardTemplate   the basic card that serves as a base for the gold card; this can be done because gold cards
     *                       are just basic cards with added features.
     * @param requirements   the resources needed on the player's board to place the card.
     *
     * @throws CardException if the requirements parameter isn't valid.
     *
     * @see BasicCard
     * @see Content
    */
    public GoldCard(BasicCard cardTemplate, List<Content> requirements) throws CardException {
        super(cardTemplate.cardId, cardTemplate.color, cardTemplate.corners, cardTemplate.points, cardTemplate.resources, cardTemplate.isFront());
        if(requirements.stream().anyMatch(c -> !c.isResource())){
            throw new CardException(String.format(
                    "The card requirements contain elements that aren't considered resources on card: %d",
                    cardTemplate.cardId)
            );
        }
        this.requirements = new ArrayList<>(requirements);
        this.owner = null;
        this.bonus = null;
    }

    /**
     * Class copy-constructor.
     *
     * @param card the instance to copy.
     */
    public GoldCard(GoldCard card){
        super(card);
        this.requirements = new ArrayList<>(card.requirements);
        this.bonus = card.bonus;
    }

    @Override
    public Map<Content,Integer> getRequirements(){
        return getMapFromContentList(requirements);
    }

    /**
     * Sets the bonus type associated to this card.
     *
     * @param bonus the card's bonus.
     *
     * @see Bonus
     * @see CornerBonus
     * @see ObjectBonus
     */
    public void setBonus(Bonus bonus){
        this.bonus = bonus;
    }

    /**
     * Override of the BasicCard method that returns the points awarded to the player when he places the card.
     * If the points are just a flat amount, returns it; if the points are related to a specific bonus type, calculates
     * the total value and then returns it.
     *
     * @return the card's awarded points.
     *
     * @see CornerBonus
     * @see ObjectBonus
     */
    @Override
    public int getPoints(){
        return bonus != null ? bonus.calculate(owner) : points;
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object card checked.
     *
     * @return       true if this card is equal to the parameter one.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof GoldCard other) {
            return super.equals(other);
        }
        return false;
    }

    /**
     * Returns a copy of this card, using the correct constructor.
     *
     * @return a copy of this card.
     */
    @Override
    public GoldCard copy(){
        return new GoldCard(this);
    }

    /**
     * Implements calculate using the amount of corners covered by the gold card
     * as the multiplier that gets applied to the card's base points.
     */
    public class CornerBonus implements Bonus{

        /**
         * Gets the total amount of points given to the player when he places the card.
         *
         * @return the card's awarded points.
         */
        @Override
        public int calculate(Player cardOwner){
            return points * cardOwner.getPlacedCards().stream()
                .filter(card -> !card.equals(GoldCard.this))
                .flatMap(card -> card.getAllCorners().stream())
                .filter(corner -> corners.stream().anyMatch(c -> c.isSamePosition(corner)))
                .mapToInt(c -> 1)
                .reduce(0, Integer::sum);
        }
    }

    /**
     * Implements calculate using the amount of specified objects found in the gold card owner's board
     * as the multiplier that gets applied to the card's base points.
     */
    public class ObjectBonus implements Bonus{
        private final Content object;

        /**
         * Class constructor.
         *
         * @param object the object required by the points' multiplier.
         */
        public ObjectBonus(Content object){
            this.object = object;
        }

        /**
         * Gets the total amount of points given to the player when he places the card.
         *
         * @return the card's awarded points.
         */
        @Override
        public int calculate(Player cardOwner){
            return points * cardOwner.getPlayerContent().get(object);
        }
    }
}