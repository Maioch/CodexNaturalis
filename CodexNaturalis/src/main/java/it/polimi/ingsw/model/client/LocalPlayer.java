package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the player associated to the local machine.
 * It saves the player's hand cards and his personal objectives.
 *
 * @see ClientPlayer
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class LocalPlayer extends ClientPlayer{

    //the player's hand.
    private List<CardSides> handCards;

    //the player's personal objectives.
    private final List<Objective> personalObjectives;

    //the cards from the player's hand that can be placed.
    private List<BasicCard> validCards;

    //the corners from the player's board where cards can be placed.
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
     * Sets the cards in the player's hand and updates the view if the show flag is true.
     *
     * @param handCards the player's hand.
     * @param show      flag that determines whether to update the view.
     */
    @Override
    public synchronized void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = new ArrayList<>(handCards);
        if(show){
            eventSubmitter.submit(() -> gameView.updateLocalPlayerHand(handCards));
        }
    }

    /**
     * Gets all the cards held by the player in his hand.
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
     * Sets the player's objectives not shared with the others and updates the view.
     *
     * @param personalObjectives the player's personal objectives.
     */
    public void setPersonalObjectives(List<Objective> personalObjectives) {
        this.personalObjectives.addAll(personalObjectives);
        eventSubmitter.submit(() -> gameView.showPersonalObjectives(personalObjectives));
    }

    /**
     * Gets the player's objectives not shared with the others.
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
     * Updates the valid cards and the valid corners of the player and
     * requests him to place a card using the event submitter.
     *
     * @param validCards   the cards that can be placed.
     * @param validCorners the corners where the player can place a new card.
     */
    public void requestCardPlacement(List<BasicCard> validCards, List<Corner> validCorners){
        this.validCards = new ArrayList<>(validCards);
        this.validCorners = new ArrayList<>(validCorners);
        List<CardSides> currentHandCards = getHandCards();
        List<BasicCard> currentPlacedCards = getPlacedCards();
        eventSubmitter.submit(() -> gameView.requestPlacement(currentHandCards, currentPlacedCards));
    }

    /**
     * Requests the player to place the starter card assigned to him using the event submitter.
     */
    public void requestStarterCardPlacement(){
        List<CardSides> currentHandCards = getHandCards();
        eventSubmitter.submit(() -> gameView.requestStarterSide(currentHandCards));
    }

    /**
     * Gets all the cards in the player's hand he can place.
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
     * Gets all the corners in the player's board he can place on.
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

    /**
     * Equals method override to fit the method to this class.
     *
     * @param obj player checked.
     *
     * @return true if this player is equal to the parameter one.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof LocalPlayer other){
            return this.getNickname().equals(other.getNickname());
        }
        return false;
    }
}