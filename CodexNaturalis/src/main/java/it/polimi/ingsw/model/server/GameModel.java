package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.server.deck.Deck;
import it.polimi.ingsw.model.server.deck.TurnDeck;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.DrawOptionsMessage;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.PlayerMessage;
import it.polimi.ingsw.network.server.ServerSubject;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.CardType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that represents a single match of Codex Naturalis.
 *
 * @author Guglielmo Gatti, Andrea Fidanza, Francesco Nisoli, Marco Maiocchi
 */
public class GameModel{
    private final ServerSubject serverSubject;
    private final List<Player> players;
    private final TurnDeck<CardSides> resourceDeck;
    private final TurnDeck<CardSides> goldDeck;
    private final Deck<CardSides> starterDeck;
    private final Deck<Objective> objectiveDeck;
    private final List<Content> availableColors;
    private final Map<String,Content> playerData;
    private final List<Objective> commonObjectives;
    private final int numberOfPlayers;

    /**
     * Constructor for the class.
     * @param numberOfPlayers the number of players requested by the creator of the game.
     * @param serverSubject the object used to notify the serverListeners.
     * @throws IllegalNumberOfPlayers if the number of players requested isn't between the minimum and maximum number
     *                                players allowed.
     */
    public GameModel(int numberOfPlayers, ServerSubject serverSubject) throws IllegalNumberOfPlayers {
        if (numberOfPlayers < GameParameters.getMinPlayers() || numberOfPlayers > GameParameters.getMaxPlayers())
            throw new IllegalNumberOfPlayers();
        this.numberOfPlayers = numberOfPlayers;
        this.serverSubject = serverSubject;
        this.availableColors = new ArrayList<>() {{
            for (Content content : Content.values()) {
                if (content.isResource()) {
                    add(content);
                }
            }
        }};
        this.players = new ArrayList<>(numberOfPlayers);
        this.playerData = new HashMap<>();
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
     * @param nickname a player's nickname.
     * @return the player in this game associated to the nickname.
     */
    public Player getPlayer(String nickname) {
        return players.stream().filter(p -> p.getNickname().equals(nickname)).findFirst().orElse(null);
    }

    /**
     * @return all the in-game players.
     */
    public List<Player> getAllPlayers() {
        return new ArrayList<>(players);
    }

    /**
     * Method that the checks if the maximum number of players is reached.
     * @return true if the game is full.
     */
    public boolean isGameFull() {
        return playerData.size() == numberOfPlayers;
    }

    public boolean isLobbyEmpty() { return playerData.isEmpty(); }

    public int getNumberOfPlayers() { return numberOfPlayers; }

    public List<String> getLobbyNicknames() { return new ArrayList<>(playerData.keySet()); }

    /**
     * Method that checks if there's a user with the same username of the new player that is joining the game.
     * @param nickname the nickname to check.
     * @return false if there's a duplicate username.
     */
    public  boolean checkNickname(String nickname) {
        return !playerData.containsKey(nickname) &&
                !nickname.contains(" ") &&
                !nickname.contains(GameParameters.getDelimiter()) &&
                !nickname.contains(GameParameters.getCommandChar()) &&
                nickname.length() <= GameParameters.getMaxNicknameLength();
    }

    /**
     * @return the list of colors that the player can choose from.
     */
    public  List<Content> getAvailableColors() {
        return new ArrayList<>(availableColors);
    }

    /**
     * Method that checks if it's the last game's turn, condition met if one of the players reaches the points cap or
     * if both decks are empty.
     * @return true if it's the last turn.
     */
    public  boolean isLastTurn() {
        return (goldDeck.isEmpty() &&
                goldDeck.getVisibleElements().isEmpty() &&
                resourceDeck.isEmpty() &&
                resourceDeck.getVisibleElements().isEmpty()) ||
                players.stream().anyMatch(p -> p.getScore() >= GameParameters.getWinThreshold());
    }

    /**
     * Method that adds a player to the game and gives him the needed cards.
     * The first card given to the player is the starterCard.
     * Finally, it notifies through the server subject the addition of the player.
     * @param nickname the nickname of the player.
     * @param color player's color.
     * @throws GameException if the color or the nickname are already taken or if the game is full.
     * @throws GameFullException if the game is full.
     * @throws NicknameTakenException if the nickname is already chosen by another player.
     */
    public void addPlayerData(String nickname, Content color) throws GameException, GameFullException, NicknameTakenException {
        if (isGameFull()) {
            throw new GameFullException();
        }
        if (!checkNickname(nickname)) {
            throw new NicknameTakenException();
        }
        if (!getAvailableColors().contains(color)) {
            throw new GameException("The chosen color has already been taken");
        }
        availableColors.remove(color);
        playerData.put(nickname,color);
        serverSubject.notify(nickname, new PlayerMessage(Status.JOIN_GAME, nickname, color));
        for(Map.Entry<String,Content> entry : playerData.entrySet()) {
            serverSubject.notifyAll(new PlayerMessage(Status.NEW_PLAYER_JOINED, entry.getKey(), entry.getValue()));
        }
    }

    public void deletePlayerData(String nickname) {
        Content color = playerData.remove(nickname);
        if(color != null){
            availableColors.add(color);
            serverSubject.notifyAll(new StringMessage(Status.PLAYER_LEFT_LOBBY, nickname));
        }
    }

    public void createPlayers(){
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

    public List<Objective> drawObjectiveCards(){
        List<Objective> result = new ArrayList<>();
        for(int i = 0; i < GameParameters.getNumberOfDrawnSecretObjectives(); i++){
            result.add(objectiveDeck.draw());
        }
        return result;
    }

    /**
     * Method that draws a card from a deck and adds it to the player's hand.
     * @param player the player that is drawing a new card.
     * @param type the type of card deck to draw from, which is either the Gold Card deck or the Resource Card deck
     * @param drawIndex the deck has a number of visible cards which the player can see. This index lets  the player
     *                  choose whether to draw a hidden card (if the index is 0) or to take one of the visible
     *                  ones (if the index is higher than 0).
     * @throws GameException if the given card type doesn't match any deck and if the given index is invalid.
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
        serverSubject.notifyAll(new DrawOptionsMessage(Status.DRAW_OPTIONS, getDrawableCards()));
    }

    /**
     * Method that gets the draw options for the player, both cards on top of the decks and visible ones, too;
     * the first element of the returned lists is always the back side of the card on top of the deck or null if
     * the deck is empty, while the rest are the visible ones.
     * @return all the cards the player can draw during his draw phase
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
     * @return a list that contains the player/s that gathered the most points.
     */
    public List<String> getWinningPlayers() {
        int max = players.stream().map(Player::getScore).max(Integer::compareTo).orElse(0);
        return players.stream().filter(p -> p.getScore() == max).map(Player::getNickname).toList();
    }

    /**
     * @return a deep copy of the game's common objectives list.
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
     * @return true if the game is in deadlock.
     */
    public  boolean isGameStuck(){
        for(Player player : players){
            if(!player.isPlayerStuck()){
                return false;
            }
        }
        return true;
    }
}