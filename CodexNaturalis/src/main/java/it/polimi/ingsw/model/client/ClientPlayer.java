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
 * Class representing the client-related player model.
 */
public abstract class ClientPlayer {
    private final String nickname;
    private List<BasicCard> placedCards;
    private final Content color;
    private int score;
    protected EventSubmitter eventSubmitter;
    protected GameView gameView;

    /**
     * Constructor for the class.
     * @param nickname hte player's nickname.
     * @param color    the player's color.
     */
    public ClientPlayer(String nickname, Content color) {
        this.nickname = nickname;
        this.color = color;
        this.score = 0;
        this.placedCards = new ArrayList<>();
    }

    /**
     * Copy-constructor of the class
     * @param clientPlayer the instance to copy.
     */
    public ClientPlayer(ClientPlayer clientPlayer) {
        this.nickname = clientPlayer.nickname;
        this.color = clientPlayer.color;
        this.placedCards = new ArrayList<>(clientPlayer.getPlacedCards());
        this.score = clientPlayer.score;
    }

    /**
     * @return the player's nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return the player's color.
     */
    public Content getColor() { return color; }

    /**
     * @return the placed card list.
     */
    public List<BasicCard> getPlacedCards() {
        return new ArrayList<>(){{
            for(BasicCard card : placedCards){
                add(card.copy());
            }
        }};
    }

    /**
     * Setter for placed cards list and the player's score.
     * @param placedCards the current placed cards layout.
     * @param score the current player's score.
     */
    public void setPlacedCards(List<BasicCard> placedCards, int score) {
        this.placedCards = new ArrayList<>(placedCards);
        this.score = score;
        eventSubmitter.submit(() -> gameView.updateBoard(nickname, getPlacedCards()));
    }

    /**
     * @return the player's score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Setter for the player's hand cards.
     * @param handCards the current player's hand.
     */
    public abstract void setHandCards(List<CardSides> handCards);

    /**
     * Setter for the client's game view, and it's associated event submitter.
     * @param gameView the client's game view.
     * @param eventSubmitter the medium used to send the player's requests to the server.
     */
    public void setViewReferences(GameView gameView, EventSubmitter eventSubmitter){
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
    }

    /**
     * Setter for the final score of the player.
     * @param scoresByObjective the map containing each objective with the associated gathered points.
     * @param finalScore the final score to set.
     */
    public void setFinalScore(Map<Objective, Integer> scoresByObjective, Integer finalScore){
        this.score = finalScore;
        eventSubmitter.submit(() -> gameView.revealFinalSummary(getNickname(), scoresByObjective, getScore()));
    }
}