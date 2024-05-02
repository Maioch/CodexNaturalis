package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.PlayerException;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.CardHandMessage;
import it.polimi.ingsw.network.messages.game.ObjectiveScoresMessage;
import it.polimi.ingsw.network.messages.game.ObjectivesMessage;
import it.polimi.ingsw.network.messages.game.PlayerBoardMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents each one of the 4 possible players in a game, each with his distinctive nickname and color,
 * and his board and hand status during the played turn. This keeps track of the score and objectives of each player, too.
 *
 * @author Marco Maiocchi, Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli
 */
public class Player {
    private final ServerSubject serverSubject;
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
     * @param handCards cards held by the player (max 3), that they can play during his turn
     * @param objectives two objectives shared by the player and a personal one
     * @param serverSubject the object used to notify the serverListeners
     */
    public Player(String nickname,
                  Content color,
                  ArrayList<CardSides> handCards,
                  ArrayList<Objective> objectives,
                  ServerSubject serverSubject){
        this.nickname = nickname;
        this.color = color;
        this.placedCards = new ArrayList<>();
        this.handCards = new ArrayList<>(handCards);
        this.objectives = new ArrayList<>(objectives);
        this.serverSubject = serverSubject;
        this.score = 0;
        //Set the owner for both the regular cards and the objectives
        for(CardSides card : this.handCards){
            card.backSide().setOwner(this);
            card.frontSide().setOwner(this);
        }
        for(Objective obj : this.objectives){
            obj.setOwner(this);
        }
    }

    /**
     * @return player's nickname
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * @return player's color
     */
    public Content getColor(){
        return this.color;
    }

    /**
     * @return player's score
     */
    public int getScore(){
        return this.score;
    }

    /**
     * @return a deep copy of the player's objectives list
     */
    public ArrayList<Objective> getObjectives(){
        return new ArrayList<>(){{
            for(Objective objective : objectives){
                add(new Objective(objective));
            }
        }};
    }

    /**
     * @return a deep copy of the player's hand
     */
    public ArrayList<CardSides> getHandCards(){
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    /**
     * @return a deep copy of the player's placed cards
     */
    public ArrayList<BasicCard> getPlacedCards(){
        return new ArrayList<>(){{
            for(BasicCard card : placedCards){
                add(card.copy());
            }
        }};
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
     * Updates the player's score by adding the points awarded by the objectives
     * and returns an array where each element is the amount of points given by
     * each objective.
     * Finally, it notifies through the server subject the updated score
     * @return an arraylist with the amount of points given by each objective
     */
    public ArrayList<Integer> awardObjectivePoints(){
        ArrayList<Integer> points = new ArrayList<>();
        for(Objective objective : this.objectives) {
            int objectiveResult = objective.checkObjective();
            points.add(objectiveResult);
            score += objectiveResult;
        }
        serverSubject.notifyAll(new ObjectivesMessage(Status.PLAYER_SCORE, this.getObjectives(), new ArrayList<>()));
        serverSubject.notifyAll(new ObjectiveScoresMessage(Status.PLAYER_SCORE,points));
        serverSubject.notifyAll(new IntegerMessage(Status.PLAYER_SCORE,score));
        return points;
    }

    /**
     * Method that checks if a card is placeable by checking if the resources required
     * are present on the player's board
     * @param cardToPlace card to check
     * @return true if the card can be placed
     */
    public boolean checkRequirements(BasicCard cardToPlace){
        HashMap<Content,Integer> requirements = cardToPlace.getRequirements();
        HashMap<Content,Integer> playerSymbols = getPlayerContent();
        return requirements.entrySet().stream().allMatch(e -> playerSymbols.get(e.getKey()) >= e.getValue());
    }

    /**
     * Method that checks if the position chosen by the player for a new card is correct,
     * assuming that the corner that has been passed is part of the player's board
     * and that the player already has the card
     * @param corner the card's corner where the new card is going to be placed
     * @return true if the card is placeable on the corner
     */
    public boolean checkIfPlaceable(Corner corner){
        //Finds all the corners where a card can't be placed and tests
        //whether one of the corners of the card is over them.
        List<Corner> cornersToCheck = placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .filter(c -> !c.getVisibility() || c.getContent().isEmpty())
                .toList();
        //checking that the corners in which the card will be placed aren't empty
        //(and, by doing that, checking that there aren't already two cards placed over the same coordinates)
        int offsetX = corner.getLocation() == Location.TR || corner.getLocation() == Location.BR ? 1 : -1;
        int offsetY = corner.getLocation() == Location.TR || corner.getLocation() == Location.TL ? 1 : -1;
        for(int x = 0; x < 2; x++){
            for(int y = 0; y < 2; y++){
                //we have to save the values into separate variables because we need them to be final
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
     * Method that checks if a certain corner is present in the player's board
     * @param corner the corner to check
     * @return true if it is present, false otherwise
     */
    public boolean isCornerPartOfBoard(Corner corner){
        return placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .toList().contains(corner);
    }

    /**
     * Method that checks if a BasicCard is present in the player's hand
     * @param card the card to check
     * @return true if present, false otherwise
     */
    public boolean isCardInHand(BasicCard card){
        return handCards.stream().anyMatch(c -> c.frontSide().equals(card) || c.backSide().equals(card));
    }

    /**
     * Method that lets the player place a card on his board
     * Finally, it notifies through the server subject the updated player's placed cards
     * @param cardToPlace the card the player chose to place
     * @param corner the corner on the card where the card is placed
     */
    public void placeCard(BasicCard cardToPlace, Corner corner){
        if(!checkRequirements(cardToPlace) || !checkIfPlaceable(corner))
            return;
        handCards.removeIf(c -> c.frontSide().equals(cardToPlace) || c.backSide().equals(cardToPlace));
        cardToPlace.place(corner);
        placedCards.add(cardToPlace);
        corner.coverCorner();
        score += cardToPlace.getPoints();
        serverSubject.notifyAll(new PlayerBoardMessage(getPlacedCards(),score));
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_CARD,getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARD,getHandCards()));
    }

    /**
     * Method that initializes each player's board by placing a starter card in the centre; throws an exception
     * if the board is already initialized.
     * Finally, it notifies through the server subject the updated player's placed cards
     * @param starterCard the starter card chosen randomly for the player
     * @exception PlayerException if there is already a placed starter card
     */
    public void placeStarterCard(BasicCard starterCard) throws PlayerException {
        if(!placedCards.isEmpty()){
            throw new PlayerException("A starter card has already been placed.");
        }
        placedCards.add(starterCard);
        Corner startCorner = new Corner(Content.WHITE, Location.TR);
        startCorner.setX(0);
        startCorner.setY(0);
        starterCard.place(startCorner);
        serverSubject.notifyAll(new PlayerBoardMessage(getPlacedCards(), score));
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_CARD,getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARD,getHandCards()));
    }

    /**
     * Method that adds a card to the player's hand.
     * Finally, it notifies through the server subject the updated player's hand
     * @param cardSides the card to add to the player's hand
     */
    public void addCardToHand(CardSides cardSides){
        handCards.add(cardSides);
        cardSides.frontSide().setOwner(this);
        cardSides.backSide().setOwner(this);
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_CARD,getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARD,getHandCards()));
    }

    /**
     * Method that gets the backside of the cards in the player's hand
     * @return the list of backsides
     */
    private ArrayList<CardSides> getBackOnlyCardHand(){
        return new ArrayList<>(){{
            for(CardSides cardSides : Player.this.getHandCards()){
                add(new CardSides(null, cardSides.backSide()));
            }
        }};
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
}