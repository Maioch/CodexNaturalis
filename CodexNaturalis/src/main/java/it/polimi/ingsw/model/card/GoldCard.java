package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Bonus;
import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Player;
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
    GoldCard(BasicCard cardTemplate, ArrayList<Content> requirements, Bonus bonus, Player owner){
        super(cardTemplate.cardId, cardTemplate.color, cardTemplate.corners, cardTemplate.points, cardTemplate.resources);
        this.requirements = (ArrayList<Content>) requirements.clone();
        this.bonus = bonus;
        this.owner = owner;
    }

    /**
     * Getter of the "requirements" parameter
     * @return the requirements needed to play the card
     */
    private ArrayList<Content> getRequirements(){
        return (ArrayList<Content>) requirements.clone();
    }

    /**
     * A method that calculates the total points value that the card gives when played (by the owner of it)
     * @return points value gained by playing the card
     */
    @Override
    public int getPoints(){
        return 0;
    }
    
}
