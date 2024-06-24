package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.GameException;
import it.polimi.ingsw.exceptions.GameFullException;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.exceptions.NicknameException;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.*;
import it.polimi.ingsw.model.server.deck.Deck;
import it.polimi.ingsw.model.server.deck.TurnDeck;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.shared.NetworkHandler;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.DrawOptionsMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.*;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents the state of a match.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class GameModel{

    //stores and notifies the networkHandlers.
    private final ServerSubject serverSubject;

    //the game's players.
    private final List<Player> players;

    //the deck from which resource cards are drawn.
    private final TurnDeck<CardSides> resourceDeck;

    //the deck from which gold cards are drawn.
    private final TurnDeck<CardSides> goldDeck;

    //the deck from which starter cards are drawn.
    private final Deck<CardSides> starterDeck;

    //the deck from which objective cards are drawn.
    private final Deck<Objective> objectiveDeck;

    //the player colors that haven't been chosen by any of the players in the game's lobby.
    private final List<Content> availableColors;

    //stores the names and the colors chosen by the players when the game hasn't started yet.
    private final Map<String, Content> playerData;

    //stores the names and the position of the players in a round of turns.
    private final Map<String, Integer> turnOrder;

    //the game's common objectives.
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private final List<Objective> commonObjectives;

    //the number of players required to start the game.
    private final int numberOfPlayers;

    //the game's id
    private final int gameId;

    /**
     * Class constructor.
     *
     * @param numberOfPlayers         the number of players requested by the creator of the game.
     * @param serverSubject           the object used to notify the network handlers.
     * @param gameId                  the id of this game.
     *
     * @throws IllegalNumberOfPlayers if the number of players requested isn't between the minimum and maximum number
     *                                allowed.
     *
     * @see ServerSubject
     * @see NetworkHandler
     */
    public GameModel(int numberOfPlayers, ServerSubject serverSubject, int gameId) throws IllegalNumberOfPlayers {
        if (numberOfPlayers < Parameters.getMinPlayers() || numberOfPlayers > Parameters.getMaxPlayers())
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
        this.turnOrder = new LinkedHashMap<>();
        int numberOfVisibleCards = Parameters.getNumberOfVisibleCards();
        this.resourceDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                Parameters.getStartCardIndex(CardType.RESOURCE),
                Parameters.getEndCardIndex(CardType.RESOURCE),
                numberOfVisibleCards);
        this.goldDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                Parameters.getStartCardIndex(CardType.GOLD),
                Parameters.getEndCardIndex(CardType.GOLD),
                numberOfVisibleCards);
        this.starterDeck = new Deck<>(
                CardBuilder::buildCard,
                Parameters.getStartCardIndex(CardType.STARTER),
                Parameters.getEndCardIndex(CardType.STARTER));
        this.objectiveDeck = new Deck<>(
                CardBuilder::buildObjective,
                Parameters.getStartCardIndex(CardType.OBJECTIVE),
                Parameters.getEndCardIndex(CardType.OBJECTIVE));
        this.commonObjectives = new ArrayList<>() {{
            for (int i = 0; i < Parameters.getNumberOfCommonObjectives(); i++) {
                add(objectiveDeck.draw());
            }
        }};
    }

    /**
     * Gets the player associated to the specified nickname.
     *
     * @param nickname the player's nickname.
     *
     * @return         the player with the given nickname.
     */
    public Player getPlayer(String nickname) {
        return players.stream().filter(p -> p.getNickname().equals(nickname)).findFirst().orElse(null);
    }

    /**
     * Gets all the players in this match.
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
     * Gets the number of players connected to this game.
     *
     * @return the players number.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * Gets the nicknames of all the players in this game.
     *
     * @return all the player's nicknames.
     */
    public synchronized List<String> getLobbyNicknames() {
        return new ArrayList<>(playerData.keySet());
    }

    /**
     * Gets the colors available to be chosen by a new player.
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
                players.stream().anyMatch(p -> p.getScore() >= Parameters.getWinThreshold());
    }

    /**
     * Adds a new player to the lobby, subscribes it to the server subject and updates all the information needed as
     * well as notifying the other players in the lobby.
     *
     * @param nickname           the player's nickname.
     * @param color              the player's color.
     * @param handler            the player's network handler.
     *
     * @throws GameException     if the color chosen by the player is already taken.
     * @throws GameFullException if the game is full.
     * @throws NicknameException if the nickname chosen is invalid.
     *
     * @see Content
     * @see NetworkHandler
     * @see ServerSubject
     */
    public synchronized void addPlayerData(String nickname, Content color, NetworkHandler handler)
            throws GameException, GameFullException, NicknameException {
        if (isGameFull()) {
            throw new GameFullException();
        }
        if (!checkNickname(nickname)) {
            throw new NicknameException();
        }
        if (!getAvailableColors().contains(color)) {
            throw new GameException("The chosen color has already been taken");
        }
        serverSubject.subscribe(nickname, handler);
        availableColors.remove(color);
        playerData.put(nickname,color);
        serverSubject.notify(nickname, new JoinGameMessage(Status.JOIN_GAME, nickname, color, numberOfPlayers, gameId));
        List<Integer> availableTurnPositions = IntStream.range(1, numberOfPlayers + 1)
                .boxed()
                .filter(n -> !turnOrder.containsValue(n))
                .toList();
        int turnNumber = availableTurnPositions.get(new Random().nextInt(availableTurnPositions.size()));
        turnOrder.put(nickname, turnNumber);
        for(Map.Entry<String, Content> entry : playerData.entrySet()) {
            serverSubject.notifyAll(
                    new JoinGameMessage(Status.NEW_PLAYER_JOINED, entry.getKey(), entry.getValue(),
                        turnOrder.get(entry.getKey()), gameId));
        }
    }

    /**
     * Checks the nickname chosen by a new player trying to join the game.
     * Checks if the nickname hasn't already been chosen by another player and doesn't contain illegal characters.
     *
     * @param nickname the nickname to check.
     *
     * @return         ture if the nickname is valid.
     */
    private synchronized boolean checkNickname(String nickname) {
        return !playerData.containsKey(nickname) &&
                !nickname.contains(" ") &&
                !nickname.contains(Parameters.getDelimiter()) &&
                !nickname.contains(Parameters.getCommandChar()) &&
                nickname.length() <= Parameters.getMaxNameLength();
    }

    /**
     * Removes the information about the player associated to the specified nickname from the lobby.
     *
     * @param nickname the player's nickname.
     */
    public synchronized void deletePlayerData(String nickname) {
        Content color = playerData.remove(nickname);
        turnOrder.remove(nickname);
        if(color != null){
            availableColors.add(color);
            serverSubject.notifyAll(new StringMessage(Status.PLAYER_LEFT_LOBBY, nickname));
        }
        serverSubject.unsubscribe(nickname);
    }

    /**
     * Initializes the players at the start of the game.
     * Gives them their starter card, hand cards and objectives and notifies the server that the player has been
     * successfully added.
     *
     * @see Player
     */
    public synchronized void createPlayers(){
        List<Map.Entry<String,Content>> sortedPlayerData = playerData.entrySet()
                .stream()
                .sorted((e1,e2) -> Integer.compare(turnOrder.get(e1.getKey()),turnOrder.get(e2.getKey())))
                .toList();
        for(Map.Entry<String,Content> entry : sortedPlayerData) {
            ArrayList<CardSides> handCards = new ArrayList<>() {{
                add(starterDeck.draw());
                for (int i = 0; i < Parameters.getNumberOfGoldCardsInHand(); i++) {
                    add(goldDeck.draw());
                }
                for (int i = 0; i < Parameters.getNumberOfResourceCardsInHand(); i++) {
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
     * Gets a set number of objective cards drawn by the objectives deck.
     *
     * @return two random objectives.
     *
     * @see Objective
     * @see Deck
     */
    public List<Objective> drawObjectiveCards(){
        List<Objective> result = new ArrayList<>();
        for(int i = 0; i < Parameters.getNumberOfDrawnSecretObjectives(); i++){
            result.add(objectiveDeck.draw());
        }
        return result;
    }

    /**
     * Draws a card from a deck and adds it to a player's hand as well as
     * notifying all the players about the new draw options.
     *
     * @param player         the player that is drawing a new card.
     * @param type           the type of card deck to draw from, which is either the Gold Card deck or the Resource Card deck.
     * @param drawIndex      the deck has a number of visible cards which the player can see. This index lets the player
     *                       choose whether to draw a hidden card (if the index is 0) or to take one of the visible
     *                       ones (if the index is higher than 0).
     *
     * @throws GameException if the given card type doesn't match any deck, if the given index is invalid or if
     *                       the chosen deck is empty.
     *
     * @see CardType
     * @see Deck
     * @see Player
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
     * Gets the draw options for the player: both cards on top of the decks and visible ones;
     * the first element of the returned lists is always the back side of the card on top of the deck or null if
     * the deck is empty, while the rest are the visible ones.
     *
     * @return all the cards the player can draw during his draw phase.
     *
     * @see Deck
     * @see CardType
     * @see BasicCard
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
     * Gets the number of cards left in both resource and gold decks, not counting visible cards.
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
     * Gets the player/s that won the game, meaning they've gathered the most amount of points.
     *
     * @return the game's winning player/s.
     */
    public List<String> getWinningPlayers() {
        int max = players.stream().map(Player::getScore).max(Integer::compareTo).orElse(0);
        return players.stream().filter(p -> p.getScore() == max).map(Player::getNickname).toList();
    }

    /**
     * Gets the objectives that the players have in common.
     * 
     * @return the game's common objectives.
     *
     * @see Objective
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