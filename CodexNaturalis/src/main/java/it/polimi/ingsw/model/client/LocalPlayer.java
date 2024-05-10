package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.ArrayList;
import java.util.List;

/**
 * A class representing the local player (the one "controlled" by the client)
 */
public class LocalPlayer extends ClientPlayer{
    private List<CardSides> handCards;
    private List<Objective> personalObjectives;

    /**
     * Constructor for the class.
     * @param nickname hte player's nickname.
     * @param color the player's color.
     */
    public LocalPlayer(String nickname, Content color) {
        super(nickname, color);
    }

    /**
     * @return a list containing all the cards in the player's hand.
     */
    public List<CardSides> getHandCards() {
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    /**
     * A setter of the local player's hand cards.
     * @param handCards the list of the new hand cards.
     */
    @Override
    public void setHandCards(List<CardSides> handCards) {
        this.handCards = handCards;
        eventSubmitter.submit(() -> gameView.updateLocalPlayerHand(getHandCards()));
    }

    /**
     * A setter for the player's objectives.
     * @param personalObjectives the list of objectives.
     */
    public void setPersonalObjectives(List<Objective> personalObjectives) {
        this.personalObjectives = new ArrayList<>(personalObjectives);
        eventSubmitter.submit(() -> gameView.updatePersonalObjectives(getPersonalObjectives()));
    }

    /**
     * Method that requests all the valid card placements and the cards that can actually be placed.
     * @param validCards the cards that can be placed.
     * @param validCorners the corners where the player can place a new card.
     */
    public void requestCardPlacement(List<BasicCard> validCards, List<Corner> validCorners){
        eventSubmitter.submit(() -> gameView.requestPlacement(getHandCards(),getPlacedCards(),validCards,validCorners));
    }

    /**
     * Method that requests the starter card assigned to the player.
     */
    public void requestStarterCardPlacement(){
        eventSubmitter.submit(() -> gameView.requestStarterSide(getHandCards()));
    }

    /**
     * A getter for the player's objectives.
     * @return the list of objectives.
     */
    public List<Objective> getPersonalObjectives() {
        return new ArrayList<>(){{
            for(Objective obj : personalObjectives){
                add(new Objective(obj));
            }
        }};

    }
}
