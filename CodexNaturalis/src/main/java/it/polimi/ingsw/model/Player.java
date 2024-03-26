package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.*;
import it.polimi.ingsw.model.card.*;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class that represents each one of the 4 possible players in a game, each with his distinctive nickname and color,
 * and his board and hand status during the played turn. This keeps track of the score and objectives of each player, too.
 *
 * @author Marco Maiocchi
 */
public class Player {
    private final String nickname;
    private final Content color;
    private final ArrayList<BasicCard> placedCards;
    private final ArrayList<BasicCard> handCards;
    private final ArrayList<Objective> objectives;
    private int score;

    /**
     * Constructor for the player
     * @param nickname in-game name for the player
     * @param color color chosen by the player
     * @param placedCards cards place by the player during the game; at first there's just the starter card
     * @param handCards cards held by the player (max 3), that he can play during his turn
     * @param objectives two objectives shared by the player and a personal one
     */
    public Player(String nickname, Content color, ArrayList<BasicCard> placedCards, ArrayList<BasicCard> handCards, ArrayList<Objective> objectives){
        this.nickname = nickname;
        this.color = color;
        this.placedCards = (ArrayList<BasicCard>) placedCards.clone();
        this.handCards = (ArrayList<BasicCard>) handCards.clone();
        this.objectives = (ArrayList<Objective>) objectives.clone();
        this.score = 0;
    }

    /**
     * @return player's nickname
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * @return player's score
     */
    public int getScore(){
        return this.score;
    }

    /**
     * @return player's placed cards
     */
    public ArrayList<BasicCard> getPlacedCards(){
        return (ArrayList<BasicCard>) this.placedCards.clone();
    }

    /**
     * @return a hash map with every possible content as key, and the corresponding quantity that is
     * visible in the player's board
     */
    public HashMap<Content,Integer> getPlayerContent(){
        return new HashMap<>(){{
        for(Content content : Content.values()){
            put(content, getPlacedCards().stream()
                    .map(BasicCard::getCardSymbols)
                    .mapToInt(x -> x.get(content))
                    .reduce(0, Integer::sum)
            );
        }}};
    }

    /**
     * @return total points to add to the player's score, given by his accomplished objectives
     */
    public int getObjectivePoints(){
        int points = 0;
        for(Objective objective : objectives)
            points += objective.checkObjective();
        return points;
    }


    /**
     * Method that guides the player during each of his turns
     * @param resourceDeck deck of the resource cards the player can draw from
     * @param goldDeck deck of the gold cards the player can draw from
     * @return the total amount of points the player gathered during his turn
     */
    public int playTurn(TurnDeck resourceDeck,TurnDeck goldDeck){
        return 0;
    }

    /**
     * Supporting method for playTurn that allows the player to place a card on his board
     * @param cardToPlace the card the player chose to place
     * @param corner the corner on the board where the card is placed
     * @return true if the card is correctly placed
     */
    private boolean placeCard(BasicCard cardToPlace, Corner corner){
        return false;
    }
}