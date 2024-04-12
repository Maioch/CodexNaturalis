package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.*;
import it.polimi.ingsw.model.card.*;

import java.awt.Point;
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
     * @param starterCard starter card given by Game
     * @param handCards cards held by the player (max 3), that he can play during his turn
     * @param objectives two objectives shared by the player and a personal one
     */
    public Player(String nickname, Content color, BasicCard starterCard, ArrayList<CardSides> handCards, ArrayList<Objective> objectives){
        this.nickname = nickname;
        this.color = color;
        this.placedCards = new ArrayList<>(){{add(starterCard);}};
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
     * Method that guides the player during each of his turns
     * @param resourceDeck deck of the resource cards the player can draw from
     * @param goldDeck deck of the gold cards the player can draw from
     * @return the total amount of points the player gathered during his turn
     */
    public int playTurn(TurnDeck resourceDeck, TurnDeck goldDeck){
        //ask the player what card he wants to play and where he wants to place it
        BasicCard cardToPlace = null;
        Corner cornerToPlaceOn = null;

        //the player is asked which card to place and where to place it

        placeCard(cardToPlace, cornerToPlaceOn);
        boolean isResourceChosen = false;
        int index = 0;

        //the player has prior knowledge of what the deck board is looking like, so he chooses if he wants a card from
        //the resource deck or the gold one and also gives the index (meaning 1 or 2 for visible cards, 0 for deck)

        CardSides drawnCard = isResourceChosen ?
                (index == 0 ? resourceDeck.draw() : resourceDeck.drawVisibleCard(index - 1)) :
                (index == 0 ? goldDeck.draw() : goldDeck.drawVisibleCard(index - 1));
        score += cardToPlace.getPoints();
        return score;
    }

    /**
     * A method which checks all the conditions that make a card correctly placeable
     * @param cardToPlace the card the player chose to place
     * @param corner the card's corner where the new card is going to be placed
     * @return true if the card is placeable on the corner
     */
    public boolean checkIfPlaceable(BasicCard cardToPlace, Corner corner){
        HashMap<Content,Integer> requirements = cardToPlace.getRequirements();
        HashMap<Content,Integer> playerSymbols = getPlayerContent();

        if(!requirements.entrySet().stream().allMatch(e -> playerSymbols.get(e.getKey()) >= e.getValue())){
            return false;
        }

        HashMap<Location, Point> offsets = new HashMap<>(){{
            put(Location.BL, new Point(-1, -1));
            put(Location.BR, new Point(1, -1));
            put(Location.TL, new Point(-1, 1));
            put(Location.TR, new Point(1, 1));
        }};
        List<Corner> cornersToCheck = placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .filter(c -> !c.getVisibility() || c.getContent() == Content.EMPTY)
                .toList();
        Point offset = offsets.get(corner.getLocation());

        //checking that the corners in which the card will be placed aren't empty
        //(and, by doing that, checking that there aren't already two cards placed over the same coordinates)
        for(Corner c : cornersToCheck){
            if(corner.getX() == c.getX() && corner.getY() == c.getY()){
                return false;
            }
            if(corner.getX() + offset.x == c.getX() && corner.getY() == c.getY()){
                return false;
            }
            if(corner.getX() == c.getX() && corner.getY() + offset.y == c.getY()){
                return false;
            }
            if(corner.getX() + offset.x  == c.getX() && corner.getY() + offset.y == c.getY()){
                return false;
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
    private void placeCard(BasicCard cardToPlace, Corner corner){
        if(!checkIfPlaceable(cardToPlace, corner))
            return;
        handCards.removeIf(c -> c.frontSide().equals(cardToPlace) || c.backSide().equals(cardToPlace));
        cardToPlace.place(corner.getX(), corner.getY());
        placedCards.add(cardToPlace);
        corner.coverCorner();
    }
}