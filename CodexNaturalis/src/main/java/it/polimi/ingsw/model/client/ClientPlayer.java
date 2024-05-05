package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class representing the client-related player model
 */
public abstract class ClientPlayer {
    private final String nickname;
    private ArrayList<BasicCard> placedCards;
    private final Content color;
    private int score;
    private EventSubmitter eventSubmitter;

    /**
     * Constructor of the class
     * @param nickname the player's nickname
     */
    public ClientPlayer(String nickname, Content color) {
        this.nickname = nickname;
        this.color = color;
        this.score = 0;
        this.placedCards = new ArrayList<>();
    }

    /**
     * Copy-constructor of the class
     * @param clientPlayer the instance to copy
     */
    public ClientPlayer(ClientPlayer clientPlayer) {
        this.nickname = clientPlayer.nickname;
        this.color = clientPlayer.color;
        this.placedCards = new ArrayList<>(clientPlayer.getPlacedCards());
        this.score = clientPlayer.score;
    }

    /**
     * Getter of the nickname attribute
     * @return the player's nickname
     */
    public String getNickname() {
        return nickname;
    }

    public Content getColor() { return color; }

    /**
     * Getter of the placed cards list
     * @return the placed card list
     */
    public ArrayList<BasicCard> getPlacedCards() {
        return new ArrayList<>(){{
            for(BasicCard card : placedCards){
                add(card.copy());
            }
        }};
    }

    /**
     * Setter of the placed cards list
     * @param placedCards the new placed cards list
     */
    public void setPlacedCards(ArrayList<BasicCard> placedCards) {
        this.placedCards = placedCards;
    }

    /**
     * Getter of the player's score
     * @return it's score
     */
    public int getScore() {
        return score;
    }

    /**
     * Setter for the player's hand
     */
    public abstract void setHandCards(ArrayList<CardSides> handCards);

    /**
     * Setter of the player's score
     * @param score new score
     */
    public void setScore(int score) {
        this.score = score;
    }

    public void setFinalScore(HashMap<Objective, Integer> scoresByObjective, Integer finalScore){
        setScore(finalScore);
    }

}
