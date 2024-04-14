package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.*;
import it.polimi.ingsw.model.card.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents each one of the 4 possible players in a game, each with his distinctive nickname and color,
 * and his board and hand status during the played turn. This keeps track of the score and objectives of each player, too.
 *
 * @author Marco Maiocchi, Andrea Fidanza
 */
public class Player {
    private final String nickname;
    private final Content color;
    private final ArrayList<BasicCard> placedCards;
    private final ArrayList<CardSides> handCards;
    private final ArrayList<Objective> objectives;
    private int score;

    /**
     * Constructor for the player
     * @param nickname in-game name for the player
     * @param color color chosen by the player
     * @param handCards cards held by the player (max 3), that he can play during his turn
     * @param objectives two objectives shared by the player and a personal one
     */
    public Player(String nickname, Content color, ArrayList<CardSides> handCards, ArrayList<Objective> objectives){
        this.nickname = nickname;
        this.color = color;
        this.placedCards = new ArrayList<>();
        this.handCards = new ArrayList<>(handCards);
        this.objectives = new ArrayList<>(objectives);
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

    public ArrayList<CardSides> getHandCards(){
        return new ArrayList<>(this.handCards);
    }
    /**
     * @return player's placed cards
     */
    public ArrayList<BasicCard> getPlacedCards(){
        return new ArrayList<>(this.placedCards);
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
        for(Objective objective : this.objectives)
            points += objective.checkObjective();
        return points;
    }

    /**
     * A method which checks all the conditions that make a card correctly placeable
     * assumes that the corner that has been passed is part of the player's board
     * @param cardToPlace the card the player chose to place
     * @param corner the card's corner where the new card is going to be placed
     * @return true if the card is placeable on the corner
     */
    public boolean checkIfPlaceable(BasicCard cardToPlace, Corner corner){
        //Verifies that the card is present in the player's hand
        if (handCards.stream()
                .noneMatch(c -> c.frontSide().equals(cardToPlace) || c.backSide().equals(cardToPlace))){
            return false;
        }
        //Verifies that the requirements for placing the cards are met
        HashMap<Content,Integer> requirements = cardToPlace.getRequirements();
        HashMap<Content,Integer> playerSymbols = getPlayerContent();
        if(!requirements.entrySet().stream().allMatch(e -> playerSymbols.get(e.getKey()) >= e.getValue())){
            return false;
        }
        //Finds all the corners where a card can't be placed and tests
        //whether one of the corners of the card is over them.
        List<Corner> cornersToCheck = placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .filter(c -> !c.getVisibility() || c.getContent() == Content.EMPTY)
                .toList();
        //checking that the corners in which the card will be placed aren't empty
        //(and, by doing that, checking that there aren't already two cards placed over the same coordinates)
        int offsetX = corner.getLocation() == Location.TR || corner.getLocation() == Location.BR ? 1 : -1;
        int offsetY = corner.getLocation() == Location.TR || corner.getLocation() == Location.TL ? 1 : -1;
        for(int x = 0; x < 2; x++){
            for(int y = 0; y < 2; y++){
                //we need to save the values into separate variables because we need them to be final
                int currentX = x;
                int currentY = y;
                if(cornersToCheck.stream()
                        .anyMatch(c -> c.getX() == corner.getX() + currentX * offsetX &&
                                c.getY() == corner.getY() + currentY * offsetY)){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object){
        if(this.getClass() != object.getClass())
            return false;
        Player other = (Player) object;
        return this.nickname.equals(other.nickname) &&
                this.color == other.color &&
                this.score == other.score &&
                this.handCards.equals(other.handCards) &&
                this.placedCards.equals(other.placedCards) &&
                this.objectives.equals(other.objectives);
    }

    /**
     * Supporting method for playTurn that allows the player to place a card on his board
     * @param cardToPlace the card the player chose to place
     * @param corner the corner on the card where the card is placed
     */
    public void placeCard(BasicCard cardToPlace, Corner corner){
        if(!checkIfPlaceable(cardToPlace, corner))
            return;
        handCards.removeIf(c -> c.frontSide().equals(cardToPlace) || c.backSide().equals(cardToPlace));
        cardToPlace.place(corner.getX(), corner.getY());
        placedCards.add(cardToPlace);
        corner.coverCorner();
        score += cardToPlace.getPoints();
    }

    public void placeStarterCard(BasicCard starterCard){
        if(!placedCards.isEmpty()){
            throw new RuntimeException("A starter card has already been placed.");
        }
        placedCards.add(starterCard);
        starterCard.place(0,0);
    }

    /**
     * method that draws a card from a deck and adds it to the player's hand.
     * @param deck the deck to draw from, which is either the Gold Card deck or the Resource Card deck
     * @param drawIndex the deck has a number of visible cards which the player can see. this index lets
     *                  the player choose whether to draw a hidden card (if the index is 0)
     *                  or to take one of the visible ones (if the index is higher than 0).
     */
    public void drawCard(TurnDeck deck, int drawIndex){
        handCards.add(drawIndex == 0 ? deck.draw() : deck.drawVisibleCard(drawIndex - 1));
    }
}