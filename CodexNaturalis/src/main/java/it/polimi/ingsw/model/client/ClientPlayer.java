package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stores the information about a player's state.
 * The main information is the nickname, the color, the cards placed and the score.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public abstract class ClientPlayer {

    //the player's position in a round of turns
    private int turnNumber;

    //the player's nickname
    private final String nickname;

    //the player's board
    private List<BasicCard> placedCards;

    //the player's chosen color
    private final Content color;

    //the player's current score
    private int score;

    //the event submitter used to update the view
    protected EventSubmitter eventSubmitter;

    //the current game view
    protected GameView gameView;

    /**
     * Class constructor.
     *
     * @param nickname   the player's nickname.
     * @param color      the player's color.
     */
    public ClientPlayer(String nickname, Content color) {
        this.nickname = nickname;
        this.color = color;
        this.score = 0;
        this.turnNumber = 0;
        this.placedCards = new ArrayList<>();
    }

    /**
     * Class copy-constructor.
     * The event submitter and the game view are ignored.
     *
     * @param clientPlayer the instance to copy.
     */
    @SuppressWarnings("CopyConstructorMissesField")
    public ClientPlayer(ClientPlayer clientPlayer) {
        this.nickname = clientPlayer.nickname;
        this.color = clientPlayer.color;
        this.placedCards = new ArrayList<>(clientPlayer.getPlacedCards());
        this.score = clientPlayer.score;
        this.turnNumber = clientPlayer.turnNumber;
    }

    /**
     * Gets the turnNumber field.
     *
     * @return the number that represents the order with which the player will play the turn
     */
    public int getTurnNumber(){
        return turnNumber;
    }

    /**
     * Sets the turnNumber field.
     *
     * @param turnNumber the number that represents the order with which the player will play the turn
     */
    public void setTurnNumber(int turnNumber){
        this.turnNumber = turnNumber;
    }

    /**
     * Gets the nickname chosen by the player.
     *
     * @return the player's nickname.
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Gets the color chosen by the player.
     *
     * @return the player's color.
     */
    public Content getColor() {
        return color;
    }

    /**
     * Gets the current cards placed by the player on his board.
     *
     * @return the placed card list.
     */
    public synchronized List<BasicCard> getPlacedCards() {
        List<BasicCard> result = new ArrayList<>();
        for(BasicCard card : placedCards){
            result.add(card.copy());
        }
        return result;
    }

    /**
     * Sets the cards placed by the player on his board and his score as well as updating the view.
     *
     * @param placedCards the player's placed cards.
     * @param score       the player's score.
     */
    public synchronized void setPlacedCards(List<BasicCard> placedCards, int score) {
        this.placedCards = new ArrayList<>(placedCards);
        this.score = score;
        List<BasicCard> currentPlacedCards = getPlacedCards();
        eventSubmitter.submit(() -> gameView.updateBoard(nickname, currentPlacedCards, score));
    }

    /**
     * Sets the cards in the player's hand and updates the view if the show flag is true.
     *
     * @param handCards the player's hand.
     * @param show      flag that determines whether to update the view.
     */
    public abstract void setHandCards(List<CardSides> handCards, boolean show);

    /**
     * Sets the client's game view and event submitter.
     *
     * @param gameView       the client's game view.
     * @param eventSubmitter the medium used to submit the changes of the player's state to the view.
     */
    public void setViewReferences(GameView gameView, EventSubmitter eventSubmitter){
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
    }

    /**
     * Sets the total points gathered by the player at the end of the game and updates the view.
     *
     * @param scoresByObjective all the objectives held by the player and the points he made by completing them.
     * @param finalScore        the player's final score.
     */
    public void setFinalScore(Map<Objective, Integer> scoresByObjective, Integer finalScore){
        this.score = finalScore;
        eventSubmitter.submit(() -> gameView.revealFinalSummary(getNickname(), scoresByObjective, score));
    }
}