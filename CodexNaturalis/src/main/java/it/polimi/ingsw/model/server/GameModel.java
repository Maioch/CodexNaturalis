package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameException;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.model.shared.card.*;
import it.polimi.ingsw.model.server.deck.Deck;
import it.polimi.ingsw.model.server.deck.TurnDeck;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.DrawOptionsMessage;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.*;

/**
 * GameModel represents a single match of Codex Naturalis.
 * This is used as the model in the MVC pattern.
 */
public class GameModel{
    private final ServerSubject serverSubject;
    private final List<Player> players;
    private final TurnDeck<CardSides> resourceDeck;
    private final TurnDeck<CardSides> goldDeck;
    private final Deck<CardSides> starterDeck;
    private final Deck<Objective> objectiveDeck;
    private final List<Content> availableColors;
    private final Map<String, Content> playerData;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Objective> commonObjectives;
    private final int numberOfPlayers;
    private final int gameId;

    /**
     * Class constructor.
     *
     * @param numberOfPlayers         the number of players requested by the creator of the game.
     * @param serverSubject           the object used to notify the serverListeners.
     *
     * @throws IllegalNumberOfPlayers if the number of players requested isn't between the minimum and maximum number
     *                                players allowed.
     */
    public GameModel(int numberOfPlayers, ServerSubject serverSubject, int gameId) throws IllegalNumberOfPlayers {
        if (numberOfPlayers < GameParameters.getMinPlayers() || numberOfPlayers > GameParameters.getMaxPlayers())
            throw new IllegalNumberOfPlayers();
        this.numberOfPlayers = numberOfPlayers;
        this.gameId = gameId;
        this.serverSubject = serverSubject;
        this.availableColors = new ArrayList<>() {{
            for (Content content : Content.values()) {
                if (content.isResource()) {
                    add(content);
                }
            }
        }};
        this.players = new ArrayList<>(numberOfPlayers);
        this.playerData = new LinkedHashMap<>();
        int numberOfVisibleCards = GameParameters.getNumberOfVisibleCards();
        this.resourceDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.RESOURCE),
                GameParameters.getEndCardIndex(CardType.RESOURCE),
                numberOfVisibleCards);
        this.goldDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.GOLD),
                GameParameters.getEndCardIndex(CardType.GOLD),
                numberOfVisibleCards);
        this.starterDeck = new Deck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(CardType.STARTER),
                GameParameters.getEndCardIndex(CardType.STARTER));
        this.objectiveDeck = new Deck<>(
                CardBuilder::buildObjective,
                GameParameters.getStartCardIndex(CardType.OBJECTIVE),
                GameParameters.getEndCardIndex(CardType.OBJECTIVE));
        this.commonObjectives = new ArrayList<>() {{
            for (int i = 0; i < GameParameters.getNumberOfCommonObjectives(); i++) {
                add(objectiveDeck.draw());
            }
        }};
    }

    /**
     * Returns the player associated to the parameter nickname.
     *
     * @param nickname the player's nickname.
     *
     * @return         the player with the given nickname.
     */
    public Player getPlayer(String nickname) {
        return players.stream().filter(p -> p.getNickname().equals(nickname)).findFirst().orElse(null);
    }

    /**
     * Returns all the players in this match.
     * 
     * @return all the players.
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Checks if the maximum number of players is reached.
     *
     * @return true if the game is full.
     */
    public synchronized boolean isGameFull() {
        return playerData.size() == numberOfPlayers;
    }

    /**
     * Checks if the game is empty, meaning no players are connected.
     *
     * @return true if the game is empty.
     */
    public synchronized boolean isLobbyEmpty() {
        return playerData.isEmpty();
    }

    /**
     * Return's the number of players connected to this game.
     *
     * @return the players number.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * Returns the nicknames of all the players in this game.
     *
     * @return all the player's nicknames.
     */
    public synchronized List<String> getLobbyNicknames() {
        return new ArrayList<>(playerData.keySet());
    }

    /**
     * Checks the nickname chosen by a new player trying to join the game.
     * Checks if the nickname hasn't already been chosen by another player and doesn't contain illegal characters.
     *
     * @param nickname the nickname to check.
     *
     * @return         ture if the nickname is valid.
     */
    public synchronized boolean checkNickname(String nickname) {
        return !playerData.containsKey(nickname) &&
                !nickname.contains(" ") &&
                !nickname.contains(GameParameters.getDelimiter()) &&
                !nickname.contains(GameParameters.getCommandChar()) &&
                nickname.length() <= GameParameters.getMaxNicknameLength();
    }

    /**
     * Returns the colors available to be chosen by a new player.
     *
     * @return the available colors.
     */
    public  List<Content> getAvailableColors() {
        return new ArrayList<>(availableColors);
    }

    /**
     * Checks if it's the last game's turn.
     * This happens when one of the players reaches the points cap or if both decks are empty, so no more cards can
     * be drawn.
     *
     * @return true if it's the last turn.
     */
    public boolean isLastTurn() {
        return (goldDeck.isEmpty() &&
                goldDeck.getVisibleElements().isEmpty() &&
                resourceDeck.isEmpty() &&
                resourceDeck.getVisibleElements().isEmpty()) ||
                players.stream().anyMatch(p -> p.getScore() >= GameParameters.getWinThreshold());
    }

    /**
     * Adds a new player to the game and updates all the information needed, such as the color chosen.
     * Also notifies through the server subject the addition of the player.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     *
     * @throws GameException     if the color chosen by the player is already taken.
     * @throws GameFullException if the game is full.
     * @throws NicknameException if the nickname chosen by the player isn't valid.
     */
    public synchronized void addPlayerData(String nickname, Content color) throws GameException, GameFullException, NicknameException {
        if (isGameFull()) {
            throw new GameFullException();
        }
        if (!checkNickname(nickname)) {
            throw new NicknameException();
        }
        if (!getAvailableColors().contains(color)) {
            throw new GameException("The chosen color has already been taken");
        }
        availableColors.remove(color);
        playerData.put(nickname,color);
        serverSubject.notify(nickname, new JoinGameMessage(Status.JOIN_GAME, nickname, color, numberOfPlayers, gameId));
        int turnNumber = 1;
        for(Map.Entry<String, Content> entry : playerData.entrySet()) {
            serverSubject.notifyAll(new JoinGameMessage(Status.NEW_PLAYER_JOINED, entry.getKey(), entry.getValue(), turnNumber, gameId));
            turnNumber++;
        }
    }

    /**
     * Removes the information about the player associated to the parameter nickname.
     *
     * @param nickname the player's nickname.
     */
    public synchronized void deletePlayerData(String nickname) {
        Content color = playerData.remove(nickname);
        if(color != null){
            availableColors.add(color);
            serverSubject.notifyAll(new StringMessage(Status.PLAYER_LEFT_LOBBY, nickname));
        }
    }

    /**
     * Initializes the players at the start of the game.
     * Gives them their starter card, hand cards and objectives and notifies the server that the player has been
     * successfully added.
     */
    public synchronized void createPlayers(){
        for(Map.Entry<String,Content> entry : playerData.entrySet()) {
            ArrayList<CardSides> handCards = new ArrayList<>() {{
                add(starterDeck.draw());
                for (int i = 0; i < GameParameters.getNumberOfGoldCardsInHand(); i++) {
                    add(goldDeck.draw());
                }
                for (int i = 0; i < GameParameters.getNumberOfResourceCardsInHand(); i++) {
                    add(resourceDeck.draw());
                }
            }};
            List<Objective> objectives = new ArrayList<>() {{
                List<Objective> commonObjectiveClones = new ArrayList<>();
                for(Objective objective : commonObjectives) {
                    commonObjectiveClones.add(new Objective(objective));
                }
                addAll(commonObjectiveClones);
            }};
            players.add(new Player(entry.getKey(), entry.getValue(), handCards, objectives, serverSubject));
        }
    }

    /**
     * Returns two random objective cards, one of which will be a player's secret objective.
     *
     * @return two random objectives.
     */
    public List<Objective> drawObjectiveCards(){
        List<Objective> result = new ArrayList<>();
        for(int i = 0; i < GameParameters.getNumberOfDrawnSecretObjectives(); i++){
            result.add(objectiveDeck.draw());
        }
        return result;
    }

    /**
     * Draws a card from a deck and adds it to a player's hand.
     *
     * @param player         the player that is drawing a new card.
     * @param type           the type of card deck to draw from, which is either the Gold Card deck or the Resource Card deck.
     * @param drawIndex      the deck has a number of visible cards which the player can see. This index lets the player
     *                       choose whether to draw a hidden card (if the index is 0) or to take one of the visible
     *                       ones (if the index is higher than 0).
     *
     * @throws GameException if the given card type doesn't match any deck, if the given index is invalid or if
     *                       the chosen deck is empty.
     */
    public void drawCard(Player player, CardType type, int drawIndex) throws GameException {
        TurnDeck<CardSides> deck = switch (type) {
            case RESOURCE -> resourceDeck;
            case GOLD -> goldDeck;
            default -> throw new GameException("The given deck type is invalid");
        };
        if(drawIndex < 0 || drawIndex > deck.getVisibleElements().size()){
            throw new GameException("Invalid draw index");
        }
        if(drawIndex == 0 && deck.isEmpty()){
            throw new GameException("The given deck is empty");
        }
        CardSides newCard = drawIndex == 0 ? deck.draw() : deck.drawVisibleElement(drawIndex - 1);
        newCard.frontSide().setOwner(player);
        newCard.backSide().setOwner(player);
        player.addCardToHand(newCard);
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, getDrawableCards(), getNumberOfCardsLeft()));
    }

    /**
     * Returns the draw options for the player: both cards on top of the decks and visible ones, too;
     * the first element of the returned lists is always the back side of the card on top of the deck or null if
     * the deck is empty, while the rest are the visible ones.
     *
     * @return all the cards the player can draw during his draw phase.
     */
    public Map<CardType, List<BasicCard>> getDrawableCards() {
        Map<CardType, List<BasicCard>> result = new HashMap<>();
        result.put(CardType.RESOURCE, new ArrayList<>() {{
            add(resourceDeck.isEmpty() ? null : resourceDeck.getElementOnTop().backSide());
            addAll(resourceDeck.getVisibleElements().stream().map(CardSides::frontSide).toList());
        }});
        result.put(CardType.GOLD, new ArrayList<>() {{
            add(goldDeck.isEmpty() ? null : goldDeck.getElementOnTop().backSide());
            addAll(goldDeck.getVisibleElements().stream().map(CardSides::frontSide).toList());
        }});
        return result;
    }

    /**
     * Returns the number of cards left in both resource and gold decks.
     *
     * @return the remaining number of cards.
     */
    public Map<CardType, Integer> getNumberOfCardsLeft(){
        Map<CardType, Integer> result = new HashMap<>();
        result.put(CardType.RESOURCE, resourceDeck.getNumberOfCardsLeft());
        result.put(CardType.GOLD, goldDeck.getNumberOfCardsLeft());
        return result;
    }

    /**
     * Returns the player/s that won the game, meaning they've gathered the most amount of points.
     *
     * @return the game's winning player/s.
     */
    public List<String> getWinningPlayers() {
        int max = players.stream().map(Player::getScore).max(Integer::compareTo).orElse(0);
        return players.stream().filter(p -> p.getScore() == max).map(Player::getNickname).toList();
    }

    /**
     * Returns the objectives that the players have in common.
     * 
     * @return the game's common objectives.
     */
    public List<Objective> getCommonObjectives() {
        return new ArrayList<>(){{
            for(Objective objective : commonObjectives){
                add(new Objective(objective));
            }
        }};
    }

    /**
     * Checks if all players have no moves available.
     *
     * @return true if the game is in deadlock.
     */
    public boolean isGameStuck(){
        for(Player player : players){
            if(!player.isPlayerStuck()){
                return false;
            }
        }
        return true;
    }
}