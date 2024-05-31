package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * ClientPlayer is an abstract class that stores the information about a player's game state.
 * The main information is the nickname, the color, the cards placed and the score.
 */
public abstract class ClientPlayer {
    private final String nickname;
    private List<BasicCard> placedCards;
    private final Content color;
    private int score;
    protected EventSubmitter eventSubmitter;
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
        this.placedCards = new ArrayList<>();
    }

    /**
     * Class copy-constructor, needed to produce an exact copy of this object.
     *
     * @param clientPlayer the instance to copy.
     */
    public ClientPlayer(ClientPlayer clientPlayer) {
        this.nickname = clientPlayer.nickname;
        this.color = clientPlayer.color;
        this.placedCards = new ArrayList<>(clientPlayer.getPlacedCards());
        this.score = clientPlayer.score;
    }

    /**
     * Returns the nickname chosen by the player.
     *
     * @return the player's nickname.
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Returns the color chosen by the player.
     *
     * @return the player's color.
     */
    public Content getColor() { return color; }

    /**
     * Returns the points gathered by the player up to a certain point in the game.
     *
     * @return the player's score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Returns the current cards placed by the player on his board.
     *
     * @return the placed card list.
     */
    public List<BasicCard> getPlacedCards() {
        List<BasicCard> result = new ArrayList<>();
        for(BasicCard card : placedCards){
            result.add(card.copy());
        }
        return result;
    }

    /**
     * Updates the cards placed by the player on his board and his score.
     *
     * @param placedCards the player's placed cards.
     * @param score       the player's score.
     */
    public void setPlacedCards(List<BasicCard> placedCards, int score) {
        this.placedCards = new ArrayList<>(placedCards);
        this.score = score;
        eventSubmitter.submit(() -> gameView.updateBoard(nickname, getPlacedCards(), score));
    }

    /**
     * Updates the cards in the player's hand.
     *
     * @param handCards the player's hand.
     */
    public abstract void setHandCards(List<CardSides> handCards, boolean show);

    /**
     * Sets the client's game view (CLI/GUI) and event submitter.
     *
     * @param gameView       the client's game view.
     * @param eventSubmitter the medium used to submit a player action to the server, mainly to update the player's view.
     */
    public void setViewReferences(GameView gameView, EventSubmitter eventSubmitter){
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
    }

    /**
     * Updates the total points gathered by the player at the end of the game.
     *
     * @param scoresByObjective all the objectives held by the player and the points he made by completing them.
     * @param finalScore        the player's final score.
     */
    public void setFinalScore(Map<Objective, Integer> scoresByObjective, Integer finalScore){
        this.score = finalScore;
        eventSubmitter.submit(() -> gameView.revealFinalSummary(getNickname(), scoresByObjective, getScore()));
    }
}