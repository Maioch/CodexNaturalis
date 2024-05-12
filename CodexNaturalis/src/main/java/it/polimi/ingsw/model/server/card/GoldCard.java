package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.server.Content;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class that represents a gold card.
 *
 * @author Francesco Saverio Nisoli, Andrea Fidanza, Guglielmo Gatti, Marco Maiocchi
 */

public class GoldCard extends BasicCard {

    private final List<Content> requirements;
    private transient Bonus bonus;
    
    /**
     * Constructor for the class.
     *
     * @param cardTemplate it's a "basic" card previously initialized, which serves as a value reference for the GoldCard instantiated.
     * @param requirements the resources needed in order to play the card.
     * @throws CardException if there are invalid requirements.
    */
    public GoldCard(BasicCard cardTemplate, List<Content> requirements) throws CardException {
        super(cardTemplate.cardId, cardTemplate.color, cardTemplate.corners, cardTemplate.points, cardTemplate.resources);
        if(requirements.stream().anyMatch(c -> !c.isResource())){
            throw new CardException(
                    String.format(
                            "The card requirements contain elements that aren't considered resources on card: %d",
                            cardTemplate.cardId)
            );
        }
        this.requirements = new ArrayList<>(requirements);
        this.owner = null;
        this.bonus = null;
    }

    /**
     * Copy-Constructor method for the GoldCard.
     * @param card the GoldCard to be copied.
     */

    public GoldCard(GoldCard card){
        super(card);
        this.requirements = new ArrayList<>(card.requirements);
        this.bonus = card.bonus;
    }

    /**
     * @return the requirements needed to play the card.
     */
    @Override
    public Map<Content,Integer> getRequirements(){
        return new HashMap<>(){{
            for(Content content : Content.values()){
                put(content, requirements.stream()
                        .filter(x -> x == content)
                        .mapToInt(x -> 1)
                        .reduce(0,Integer::sum));
            }
        }};
    }

    /**
     * Setter for the bonus attribute.
     * @param bonus the bonus related to the card.
     */
    public void setBonus(Bonus bonus){
        this.bonus = bonus;
    }

    /**
     * @return points gained by placing the card.
     */
    @Override
    public int getPoints(){
        return bonus != null ? bonus.calculate() : points;
    }

    /**
    * Equals method.
    * @param object Object to check.
    * @return true if each field is equal to each field of object.
    */
    @Override
    public boolean equals(Object object){
        if(this.getClass() != object.getClass())
            return false;
        GoldCard other = (GoldCard) object;
        return super.equals(other);
    }

    /**
     * Copy method that Guarantees that the card will be copied using the right constructor.
     * @return a copy of the card.
     */
    @Override
    public GoldCard copy(){
        return new GoldCard(this);
    }

    /**
     * Class that calculates the bonus given by a gold card that gives points per corner covered by the card itself.
     *
     * @author Andera Fidanza
     */
    public class CornerBonus implements Bonus{

        /**
         * @return points gained by placing the card.
         */
        @Override
        public int calculate(){
            return points * owner.getPlacedCards().stream()
                .filter(card -> !card.equals(GoldCard.this))
                .flatMap(card -> card.getAllCorners().stream())
                .filter(corner -> corners.stream().anyMatch(c -> c.isSamePosition(corner)))
                .mapToInt(c -> 1)
                .reduce(0, Integer::sum);
        }
    }

    /**
     * Class that calculates the bonus given by a gold card that gives points per visible object in the board of the
     * player, including the card itself.
     *
     * @author Marco Maiocchi
     */
    public class ObjectBonus implements Bonus{
        private final Content object;

        /**
         * Constructor for the class.
         * @param object object used for the points' multiplier.
         */
        public ObjectBonus(Content object){
            this.object = object;
        }

        /**
         * Method that calculates the points given to the player by the gold card he plays.
         * @return points given to the player.
         */
        @Override
        public int calculate(){
            return points * owner.getPlayerContent().get(object);
        }
    }
}