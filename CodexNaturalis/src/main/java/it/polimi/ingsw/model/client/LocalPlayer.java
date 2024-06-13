package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;

import java.util.ArrayList;
import java.util.List;

/**
 * LocalPlayer is the player associated to the local machine.
 * It saves the player's hand cards and his personal objectives.
 */
public class LocalPlayer extends ClientPlayer{

    private List<CardSides> handCards;
    private final List<Objective> personalObjectives;
    private List<BasicCard> validCards;
    private List<Corner> validCorners;

    /**
     * Class constructor.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     */
    public LocalPlayer(String nickname, Content color) {
        super(nickname, color);
        personalObjectives = new ArrayList<>();
        validCorners = new ArrayList<>();
        validCards = new ArrayList<>();
        handCards = new ArrayList<>();
    }

    /**
     * Updates all the cards held by the player in his hand.
     *
     * @param handCards the player's hand cards.
     */
    @Override
    public synchronized void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = new ArrayList<>(handCards);
        if(show){
            eventSubmitter.submit(() -> gameView.updateLocalPlayerHand(getHandCards()));
        }
    }

    /**
     * Returns all the cards held by the player in his hand.
     *
     * @return the player's hand.
     */
    public synchronized List<CardSides> getHandCards() {
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    /**
     * Sets the player's objectives not shared with the others.
     *
     * @param personalObjectives the player's personal objectives.
     */
    public void setPersonalObjectives(List<Objective> personalObjectives) {
        this.personalObjectives.addAll(personalObjectives);
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
        this.validCards = new ArrayList<>(validCards);
        this.validCorners = new ArrayList<>(validCorners);
        eventSubmitter.submit(() -> gameView.requestPlacement(getHandCards(), getPlacedCards()));
    }

    /**
     * Requests the starter card assigned to the player.
     */
    public void requestStarterCardPlacement(){
        eventSubmitter.submit(() -> gameView.requestStarterSide(getHandCards()));
    }

    /**
     * Returns all the cards in the player's hand he can place.
     *
     * @return the player's placeable cards.
     */
    public List<BasicCard> getValidCards(){
        return new ArrayList<>(){{
            for(BasicCard card : validCards){
                add(card.copy());
            }
        }};
    }

    /**
     * Returns all the corners in the player's board he can place on.
     *
     * @return the corners where cards can be placed.
     */
    public List<Corner> getValidCorners(){
        return new ArrayList<>(){{
            for(Corner corner : validCorners){
                add(new Corner(corner));
            }
        }};
    }
}