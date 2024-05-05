package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;

import java.util.ArrayList;

/**
 * A class representing the local player (the one "controlled" by the client)
 */
public class LocalPlayer extends ClientPlayer{
    private ArrayList<CardSides> handCards;
    private ArrayList<Objective> personalObjectives;

    /**
     * Constructor of the class
     * @param nickname the player's game
     */
    public LocalPlayer(String nickname, Content color) {
        super(nickname, color);
    }

    public ArrayList<CardSides> getHandCards() {
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    /**
     * A setter of the local player's hand cards
     * @param handCards the list of hand cards
     */
    @Override
    public void setHandCards(ArrayList<CardSides> handCards) {
        this.handCards = handCards;
    }

    /**
     * A setter of the player objectives
     * @param personalObjectives the list of objectives
     */
    public void setPersonalObjectives(ArrayList<Objective> personalObjectives) {
        this.personalObjectives = personalObjectives;
    }

    /**
     * A getter of the player's objectives
     * @return the list of objectives
     */
    public ArrayList<Objective> getObjectives() {
        return personalObjectives;
    }
}
