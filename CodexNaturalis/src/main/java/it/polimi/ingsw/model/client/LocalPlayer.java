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
     * Constructor of the class
     * @param nickname the player's game
     */
    public LocalPlayer(String nickname, Content color) {
        super(nickname, color);
    }

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
     * A setter of the local player's hand cards
     * @param handCards the list of hand cards
     */
    @Override
    public void setHandCards(List<CardSides> handCards) {
        this.handCards = handCards;
        eventSubmitter.submit(() -> gameView.updateLocalPlayerHand(getHandCards()));
    }

    /**
     * A setter of the player objectives
     * @param personalObjectives the list of objectives
     */
    public void setPersonalObjectives(List<Objective> personalObjectives) {
        this.personalObjectives = new ArrayList<>(personalObjectives);
        eventSubmitter.submit(() -> gameView.updatePersonalObjectives(getPersonalObjectives()));
    }

    public void requestCardPlacement(List<BasicCard> validCards,
                                     List<Corner> validCorners){
        eventSubmitter.submit(() -> gameView.requestPlacement(getHandCards(),getPlacedCards(),validCards,validCorners));
    }

    public void requestStarterCardPlacement(){
        eventSubmitter.submit(() -> gameView.requestStarterSide(getHandCards()));
    }

    /**
     * A getter of the player's objectives
     * @return the list of objectives
     */
    public List<Objective> getPersonalObjectives() {
        return new ArrayList<>(){{
            for(Objective obj : personalObjectives){
                add(new Objective(obj));
            }
        }};

    }
}
