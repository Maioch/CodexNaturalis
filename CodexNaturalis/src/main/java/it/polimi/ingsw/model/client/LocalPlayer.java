package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.ArrayList;
import java.util.List;

/**
 * LocalPlayer is the player associated to the local machine.
 * It saves the player's hand cards and his personal objectives.
 */
public class LocalPlayer extends ClientPlayer{

    private List<CardSides> handCards;
    private List<Objective> personalObjectives;

    /**
     * Class constructor.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     */
    public LocalPlayer(String nickname, Content color) {
        super(nickname, color);
    }

    /**
     * Returns all the cards held by the player in his hand.
     *
     * @return the player's hand.
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
     * Updates all the cards held by the player in his hand.
     *
     * @param handCards the player's hand cards.
     */
    @Override
    public void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = handCards;
        if(show){
            eventSubmitter.submit(() -> gameView.updateLocalPlayerHand(getHandCards()));
        }
    }

    /**
     * Sets the player's objectives not shared with the others.
     *
     * @param personalObjectives the player's personal objectives.
     */
    public void setPersonalObjectives(List<Objective> personalObjectives) {
        this.personalObjectives = new ArrayList<>(personalObjectives);
        eventSubmitter.submit(() -> gameView.showPersonalObjectives(getPersonalObjectives()));
    }

    /**
     * Returns the player's objectives not shared with the others.
     *
     * @return the player's personal objectives.
     */
    public List<Objective> getPersonalObjectives() {
        return new ArrayList<>(){{
            for(Objective obj : personalObjectives){
                add(new Objective(obj));
            }
        }};
    }

    /**
     * Requests all the valid card placements and the cards that can actually be placed.
     *
     * @param validCards   the cards that can be placed.
     * @param validCorners the corners where the player can place a new card.
     */
    public void requestCardPlacement(List<BasicCard> validCards, List<Corner> validCorners){
        eventSubmitter.submit(() -> gameView.requestPlacement(getHandCards(),getPlacedCards(),validCards,validCorners));
    }

    /**
     * Requests the starter card assigned to the player.
     */
    public void requestStarterCardPlacement(){
        eventSubmitter.submit(() -> gameView.requestStarterSide(getHandCards()));
    }
}