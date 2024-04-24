package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.card.BasicCard;
import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.Objective;
import it.polimi.ingsw.model.deck.*;
import it.polimi.ingsw.model.card.CardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class that represents a single match of Codex Naturalis
 *
 * @author Guglielmo Gatti, Andrea Fidanza, Francesco Nisoli, Marco Maiocchi
 */
public class GameModel {
    private final ArrayList<Player> players;
    private final TurnDeck<CardSides> resourceDeck;
    private final TurnDeck<CardSides> goldDeck;
    private final Deck<CardSides> starterDeck;
    private final Deck<Objective> objectiveDeck;
    private final ArrayList<Content> availableColors;
    private final ArrayList<Objective> commonObjectives;
    private final int numberOfPlayers;

    /**
     * Constructor for the class
     *
     * @param numberOfPlayers the number of players requested by the creator of the game
     * @throws IllegalNumberOfPlayers if the number of players requested isn't between the minimum and maximum number
     *                                players allowed
     */
    public GameModel(int numberOfPlayers) throws IllegalNumberOfPlayers {
        if (numberOfPlayers < GameParameters.getMinPlayers() || numberOfPlayers > GameParameters.getMaxPlayers())
            throw new IllegalNumberOfPlayers();
        this.numberOfPlayers = numberOfPlayers;
        availableColors = new ArrayList<>() {{
            for (Content content : Content.values()) {
                if (content.isResource()) {
                    add(content);
                }
            }
        }};
        players = new ArrayList<>(numberOfPlayers);
        int numberOfVisibleCards = GameParameters.getNumberOfVisibleCards();
        resourceDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.RESOURCE),
                GameParameters.getEndCardIndex(CardType.RESOURCE),
                numberOfVisibleCards);
        goldDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.GOLD),
                GameParameters.getEndCardIndex(CardType.GOLD),
                numberOfVisibleCards);
        starterDeck = new Deck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.STARTER),
                GameParameters.getEndCardIndex(CardType.STARTER));
        objectiveDeck = new Deck<>(
                CardBuilder::buildObjective,
                GameParameters.getStartCardIndex(CardType.OBJECTIVE),
                GameParameters.getEndCardIndex(CardType.OBJECTIVE));
        commonObjectives = new ArrayList<>() {{
            for (int i = 0; i < GameParameters.getNumberOfCommonObjectives(); i++) {
                add(objectiveDeck.draw());
            }
        }};
    }

    /**
     * Gets the player with the specified nickname
     *
     * @param nickname the specified nickname
     * @return the player or null if there are no players with that nickname
     */
    public Player getPlayer(String nickname) {
        return players.stream().filter(p -> p.getNickname().equals(nickname)).findFirst().orElse(null);
    }

    /**
     * @return all the players
     */
    public ArrayList<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Method that the checks if the maximum number of players is reached
     *
     * @return true if the game is full
     */
    public boolean isGameFull() {
        return players.size() == numberOfPlayers;
    }

    /**
     * Method that checks if there's a user with the same username of the new player that is joining the game
     *
     * @param nickname the nickname to check
     * @return false if there's a duplicate username
     */
    public boolean checkNickname(String nickname) {
        return players.stream().noneMatch(p -> p.getNickname().equals(nickname));
    }

    /**
     * Method that gets the remaining colors that a player can choose when entering a game
     *
     * @return the list of colors that the player can choose from
     */
    public ArrayList<Content> getAvailableColors() {
        return new ArrayList<>(availableColors);
    }

    /**
     * Method that checks if it's the last game's turn, condition met if one of the players reaches the points cap or
     * if both decks are empty
     *
     * @return true if it's the last turn
     */
    public boolean isLastTurn() {
        return (goldDeck.isEmpty() &&
                goldDeck.getVisibleElements().isEmpty() &&
                resourceDeck.isEmpty() &&
                resourceDeck.getVisibleElements().isEmpty()) ||
                players.stream().anyMatch(p -> p.getScore() >= GameParameters.getWinThreshold());
    }

    /**
     * Method that adds a player to an existing game
     *
     * @param nickname the nickname of the player
     * @param color    player's color
     * @return the starter card assigned to the player
     * @throws GameException          if the color or the nickname are already taken or if the game is full
     * @throws GameFullException      if the game is full
     * @throws NicknameTakenException if the nickname is already chosen by another player
     */
    public CardSides addPlayer(String nickname, Content color) throws GameException, GameFullException, NicknameTakenException {
        if (isGameFull()) {
            throw new GameFullException();
        }
        if (!checkNickname(nickname)) {
            throw new NicknameTakenException();
        }
        if (!getAvailableColors().contains(color)) {
            throw new GameException("The chosen color has already been taken");
        }
        ArrayList<CardSides> handCards = new ArrayList<>() {{
            for (int i = 0; i < GameParameters.getNumberOfGoldCardsInHand(); i++) {
                add(goldDeck.draw());
            }
            for (int i = 0; i < GameParameters.getNumberOfResourceCardsInHand(); i++) {
                add(resourceDeck.draw());
            }
        }};
        ArrayList<Objective> objectives = new ArrayList<>() {{
            addAll(commonObjectives);
            for (int i = 0; i < GameParameters.getNumberOfSecretObjectives(); i++) {
                add(objectiveDeck.draw());
            }
        }};
        players.add(new Player(nickname, color, handCards, objectives));
        availableColors.remove(color);
        return starterDeck.draw();
    }

    /**
     * Method that draws a card from a deck and adds it to the player's hand.
     *
     * @param type      the type of card deck to draw from, which is either the Gold Card deck or the Resource Card deck
     * @param drawIndex the deck has a number of visible cards which the player can see. this index lets
     *                  the player choose whether to draw a hidden card (if the index is 0)
     *                  or to take one of the visible ones (if the index is higher than 0).
     * @throws GameException if the given card type doesn't match any deck and if the given index is invalid
     */
    public void drawCard(Player player, CardType type, int drawIndex) throws GameException {
        TurnDeck<CardSides> deck = switch (type) {
            case RESOURCE -> resourceDeck;
            case GOLD -> goldDeck;
            default -> throw new GameException("The given deck type is invalid");
        };
        if(drawIndex <= deck.getVisibleElements().size()){
            throw new GameException("Invalid draw index");
        }
        if(drawIndex == 0 && deck.isEmpty()){
            throw new GameException("The given deck is empty");
        }
        CardSides newCard = drawIndex == 0 ? deck.draw() : deck.drawVisibleElement(drawIndex - 1);
        newCard.frontSide().setOwner(player);
        newCard.backSide().setOwner(player);
        player.addCardToHand(newCard);
    }

    /**
     * Method that gets the draw options for the player, both cards on top of the decks and visible ones, too;
     * the first element of the returned lists is always the back side of the card on top of the deck or null if
     * the deck is empty, while the rest are the visible ones.
     *
     * @return all the cards the player can draw during his draw phase
     */
    public HashMap<CardType, ArrayList<BasicCard>> getDrawableCards() {
        return new HashMap<>() {{
            put(CardType.RESOURCE, new ArrayList<>() {{
                add(resourceDeck.isEmpty() ? null : resourceDeck.getElementOnTop().backSide());
                addAll(resourceDeck.getVisibleElements().stream().map(CardSides::frontSide).toList());
            }});
            put(CardType.GOLD, new ArrayList<>() {{
                add(goldDeck.isEmpty() ? null : goldDeck.getElementOnTop().backSide());
                addAll(goldDeck.getVisibleElements().stream().map(CardSides::frontSide).toList());
            }});
        }};
    }

    /**
     * Method that returns the player/s with the most points
     *
     * @return a list of said players
     */
    public List<String> getWinningPlayers() {
        int max = players.stream().map(Player::getScore).max(Integer::compareTo).orElse(0);
        return players.stream().filter(p -> p.getScore() == max).map(Player::getNickname).toList();
    }
}