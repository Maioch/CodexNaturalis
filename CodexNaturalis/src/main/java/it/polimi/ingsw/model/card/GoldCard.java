package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Bonus;
import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Player;

/**
 * A class that represents a gold card
 * Used to extend (and so, specify) BasicCard
 * @author Francesco Saverio Nisoli
 */

public class GoldCard extends BasicCard {
    private final Content[] requirements;
    private final Bonus bonus;
    private final Player owner;

    
    /**
     * @param cardTemplate it's a "basic" card previously initialized, which serves as a value reference for the GoldCard instantiated
     * @param requirements the resources needed to be able to play the card
     * @param bonus the bonus object which the card possesses
     * @param owner the player who owns the card
    */
    GoldCard(BasicCard cardTemplate, Content[] requirements, Bonus bonus, Player owner){
        super(cardTemplate.cardId, cardTemplate.color, cardTemplate.corners, cardTemplate.points, cardTemplate.resources);
        this.requirements = requirements;
        this.bonus = bonus;
        this.owner = owner;
    }

    /**
     * Getter of the "requirements" parameter
     * @return the requirements needed to play the card
     */
    private Content[] getRequirements(){
        return requirements;
    }

    /**
     * Getter of the "owner" parameter
     * @return the owner (Player) of the card
     */
    private Player getOwner(){
        return owner;
    }

    /**
     * A method that calculates the total points value tha the card gives when played (by the owner of it)
     * @return points value gained by playing the card
     */
    public int getPoints(){
        return 0;
    }
    
}
