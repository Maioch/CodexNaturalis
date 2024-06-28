package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.CardHandMessage;
import it.polimi.ingsw.network.shared.messages.game.ObjectivesMessage;
import it.polimi.ingsw.network.shared.messages.game.PlayerBoardMessage;
import it.polimi.ingsw.network.shared.messages.game.PlayerSummaryMessage;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a player in a game.
 * It saves the nickname, the color, his board and hand status during the played turn.
 * This keeps track of the score and objectives of each player, too.
 * All the methods that update on of the above values, also notify the change to the controller through the server subject.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Player {

    //stores and notifies the exchangeHandlers.
    private final ServerSubject serverSubject;

    //the player's nickname.
    private final String nickname;

    //the player's chosen color.
    private final Content color;

    //the player's board.
    private final List<BasicCard> placedCards;

    //the player's hand.
    private final List<CardSides> handCards;

    //the player's objectives, includes both the common and personal ones.
    private final List<Objective> objectives;

    //the player's current score.
    private int score;

    /**
     * Class constructor.
     *
     * @param nickname      the player's in-game name.
     * @param color         the player's chosen color.
     * @param handCards     the cards held by the player in his hand.
     * @param objectives    the player's objectives for his current game.
     * @param serverSubject the object used to notify about a change in the game's model.
     *
     * @see Content
     * @see CardSides
     * @see Objective
     * @see ServerSubject
     */
    public Player(String nickname,
                  Content color,
                  List<CardSides> handCards,
                  List<Objective> objectives,
                  ServerSubject serverSubject){
        this.nickname = nickname;
        this.color = color;
        this.placedCards = new ArrayList<>();
        this.handCards = new ArrayList<>(handCards);
        this.objectives = new ArrayList<>(objectives);
        this.serverSubject = serverSubject;
        this.score = 0;
        for(CardSides card : this.handCards){
            card.backSide().setOwner(this);
            card.frontSide().setOwner(this);
        }
        for(Objective obj : this.objectives){
            obj.setOwner(this);
        }
    }

    /**
     * Gets the nickname chosen by the player.
     *
     * @return the player's nickname.
     */
    public String getNickname(){
        return this.nickname;
    }

    /**
     * Gets the color chosen by the player.
     *
     * @return the player's color.
     */
    public Content getColor(){
        return this.color;
    }

    /**
     * Gets the player's score.
     *
     * @return the player's score.
     */
    public int getScore(){
        return this.score;
    }

    /**
     * Gets all the player's objectives, both common and personal.
     *
     * @return the player's objectives.
     *
     * @see Objective
     */
    public List<Objective> getObjectives(){
        return new ArrayList<>(){{
            for(Objective objective : objectives){
                add(new Objective(objective));
            }
        }};
    }

    /**
     * Gets the cards held by the player in his hand.
     *
     * @return the player's hand cards.
     *
     * @see CardSides
     */
    public List<CardSides> getHandCards(){
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    /**
     * Gets the cards placed by the player on his board.
     *
     * @return the player's placed cards.
     *
     * @see BasicCard
     */
    public List<BasicCard> getPlacedCards(){
        return new ArrayList<>(){{
            for(BasicCard card : placedCards){
                add(card.copy());
            }
        }};
    }

    /**
     * Gets a list of all the symbols present on the player's board, each with its corresponding number of
     * occurrences.
     *
     * @return the player's contents.
     *
     * @see Content
     */
    public Map<Content,Integer> getPlayerContent(){
        Map<Content, Integer> result = new HashMap<>();
        for(Content content : Content.values()){
            result.put(content, getPlacedCards().stream()
                    .map(BasicCard::getCardSymbols)
                    .mapToInt(x -> x.get(content))
                    .reduce(0, Integer::sum)
            );
        }
        return result;
    }

    /**
     * Updates the player's score by adding the points awarded by his objectives and returns a list where each element
     * is the amount of points given by each objective.
     *
     * @return a list with the amount of points given by each objective.
     *
     * @see Objective
     */
    public List<Integer> awardObjectivePoints(){
        Map<Objective, Integer> objectivePoints = new LinkedHashMap<>();
        for(Objective objective : this.objectives) {
            int objectiveResult = objective.checkObjective();
            objectivePoints.put(objective, objectiveResult);
            score += objectiveResult;
        }
        serverSubject.notifyAll(new PlayerSummaryMessage(objectivePoints, score, nickname));
        return new ArrayList<>(objectivePoints.values());
    }

    /**
     * Checks if a card is placeable by checking if its required resources are present on the player's board.
     *
     * @param cardToPlace the card to check.
     *
     * @return            true if the card can be placed on the player's board.
     *
     * @see BasicCard
     */
    public boolean checkRequirements(BasicCard cardToPlace){
        Map<Content,Integer> requirements = cardToPlace.getRequirements();
        Map<Content,Integer> playerSymbols = getPlayerContent();
        return requirements.entrySet().stream().allMatch(e -> playerSymbols.get(e.getKey()) >= e.getValue());
    }

    /**
     * Checks if the position chosen by the player for a new card is valid, assuming that the corner that has been
     * passed as parameter is part of the player's board and that the player has the card in his hand.
     *
     * @param corner the card's corner where the new card is going to be placed.
     *
     * @return       true if the card is placeable on the given corner.
     *
     * @see Corner
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
        return getAllCoveredCorners(corner, cornersToCheck).isEmpty();
    }

    /**
     * Gets all the already covered corners.
     * This is needed for a card placement check.
     * In order to execute the desired procedure, the cornersToCheck must contain either empty or not visible corners.
     *
     * @param corner         the card's corner where the new card is going to be placed.
     * @param cornersToCheck the list of corners that have to be checked.
     *
     * @return               the list of covered corners.
     *
     * @see Corner
     */
    private List<Corner> getAllCoveredCorners(Corner corner, List<Corner> cornersToCheck){
        List<Corner> corners = new ArrayList<>();
        int offsetX = corner.getLocation() == Location.TR || corner.getLocation() == Location.BR ? 1 : -1;
        int offsetY = corner.getLocation() == Location.TR || corner.getLocation() == Location.TL ? 1 : -1;
        for(int x = 0; x < 2; x++){
            for(int y = 0; y < 2; y++){
                //we have to save the values into separate variables because we need them to be final
                int currentX = x;
                int currentY = y;
                cornersToCheck.stream()
                        .filter(c -> c.getX() == corner.getX() + currentX * offsetX &&
                                c.getY() == corner.getY() + currentY * offsetY)
                        .findFirst()
                        .ifPresent(corners::add);
            }
        }
        return corners;
    }

    /**
     * Checks if a certain corner is present in the player's board.
     * This is useful to check if a player's move is valid.
     *
     * @param corner the corner to check.
     *
     * @return       true if present, false otherwise.
     *
     * @see Corner
     */
    public boolean isCornerPartOfBoard(Corner corner){
        return placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .toList().contains(corner);
    }

    /**
     * Checks if a BasicCard is present in the player's hand.
     * This is useful to check if a player's move is valid and for testing purposes.
     *
     * @param card the card to check.
     *
     * @return     true if present, false otherwise.
     *
     * @see BasicCard
     */
    public boolean isCardInHand(BasicCard card){
        return handCards.stream().anyMatch(c -> c.frontSide().equals(card) || c.backSide().equals(card));
    }

    /**
     * Places a new card on a player's board, if the placement specified is legal.
     *
     * @param cardToPlace the card the player wants to place.
     * @param corner      the corner where the new card will be placed.
     *
     * @see BasicCard
     * @see Corner
     */
    public void placeCard(BasicCard cardToPlace, Corner corner){
        if(!checkRequirements(cardToPlace) || !checkIfPlaceable(corner))
            return;
        handCards.removeIf(c -> c.frontSide().equals(cardToPlace) || c.backSide().equals(cardToPlace));
        for(Corner c : getAllCoveredCorners(corner, placedCards.stream().flatMap(b -> b.getAllCorners().stream()).toList())) {
            for (BasicCard card : placedCards) {
                card.coverCornerIfPresent(c);
            }
        }
        cardToPlace.place(corner);
        placedCards.add(cardToPlace);
        score += cardToPlace.getPoints();
        serverSubject.notifyAll(new PlayerBoardMessage(getPlacedCards(), score));
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_BACK, getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARDS, getHandCards()));
    }

    /**
     * Places a starter card chosen on the player's board.
     * The parameter is the side chosen by the player when he's prompted to do so.
     *
     * @param starterCard    the starter card.
     *
     * @throws CardException if there is already a placed starter card.
     *
     * @see BasicCard
     */
    public void placeStarterCard(BasicCard starterCard) throws CardException {
        if(!placedCards.isEmpty()){
            throw new CardException("A starter card has already been placed.");
        }
        placedCards.add(starterCard);
        Corner startCorner = new Corner(Content.WHITE, Location.TR);
        startCorner.setX(0);
        startCorner.setY(0);
        starterCard.place(startCorner);
        handCards.removeIf(c -> c.frontSide().equals(starterCard) || c.backSide().equals(starterCard));
        serverSubject.notifyAll(new PlayerBoardMessage(getPlacedCards(), score));
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_BACK, getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARDS, getHandCards()));
    }

    /**
     * Adds a new card to the player's hand.
     *
     * @param cardSides the card to add to the player's hand.
     *
     * @see CardSides
     */
    public void addCardToHand(CardSides cardSides){
        handCards.add(cardSides);
        cardSides.frontSide().setOwner(this);
        cardSides.backSide().setOwner(this);
        serverSubject.notifyAll(new CardHandMessage(Status.PLAYER_HAND_BACK, getBackOnlyCardHand()));
        serverSubject.notify(nickname, new CardHandMessage(Status.PLAYER_HAND_CARDS, getHandCards()));
    }

    /**
     * Adds a list of personal objectives to the player.
     *
     * @param personalObjectives the objectives to add to the player.
     *
     * @see Objective
     */
    public void addPersonalObjectives(List<Objective> personalObjectives){
        List<Objective> commonObjectives = getObjectives();
        for(Objective objective : personalObjectives){
            objective.setOwner(this);
        }
        objectives.addAll(personalObjectives);
        List<Objective> secretObjectives = getObjectives().stream()
                .filter(o -> !commonObjectives.contains(o))
                .collect(Collectors.toCollection(ArrayList::new));
        serverSubject.notify(getNickname(),
                new ObjectivesMessage(Status.SECRET_OBJECTIVES, secretObjectives));
    }

    /**
     * Gets all the valid corners.
     *
     * @return all the corners the player can place a card on.
     *
     * @see Corner
     */
    public List<Corner> getAllValidCorners(){
        return placedCards.stream()
                .flatMap(b -> b.getAllCorners().stream())
                .filter(this::checkIfPlaceable)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets all the valid cards.
     *
     * @return all the cards n the player's hand that he can place.
     *
     * @see BasicCard
     */
    public List<BasicCard> getAllValidCards(){
        List<BasicCard> result = handCards.stream()
                .map(CardSides::frontSide)
                .filter(this::checkRequirements)
                .collect(Collectors.toCollection(ArrayList::new));
        result.addAll(handCards.stream()
                .map(CardSides::backSide)
                .collect(Collectors.toCollection(ArrayList::new)));
        return result;
    }

    /**
     * Checks if the player han no legal placements.
     *
     * @return true if the player has no moves available.
     */
    public boolean isPlayerStuck(){
        return getAllValidCorners().isEmpty();
    }

    /**
     * Gets all the back sides cards in the player's hand.
     *
     * @return the list of the back sides of the player's hand.
     *
     * @see CardSides
     */
    public List<CardSides> getBackOnlyCardHand(){
        return new ArrayList<>(){{
            for(CardSides cardSides : Player.this.getHandCards()){
                add(new CardSides(null, cardSides.backSide()));
            }
        }};
    }


    /**
     * Equals method.
     *
     * @param object object to check.
     *
     * @return       true if each field is equals to each field of object.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof Player other) {
            return this.nickname.equals(other.nickname);
        }
        return false;
    }
}