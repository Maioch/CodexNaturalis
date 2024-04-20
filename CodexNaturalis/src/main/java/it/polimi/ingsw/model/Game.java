package it.polimi.ingsw.model;

import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.Objective;
import it.polimi.ingsw.model.deck.*;
import it.polimi.ingsw.model.exceptions.IllegalNumberOfPlayers;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Class that represents a single match of Codex Naturalis
 *
 * @author Guglielmo Gatti
 */
public class Game {
    public final ArrayList<Player> players;
    public final TurnDeck<CardSides> resourceDeck;
    public final TurnDeck<CardSides> goldDeck;
    public final Deck<CardSides> starterDeck;
    public final Deck<Objective> objectiveDeck;
    public final ArrayList<Content> availableColors;
    public final ArrayList<Objective> commonObjectives;
    public final int numberOfPlayers;
    public boolean isLastTurn;

    public Game(int numberOfPlayers) throws IllegalNumberOfPlayers {
        if(numberOfPlayers < GameParameters.getMinPlayers() || numberOfPlayers > GameParameters.getMaxPlayers())
            throw new IllegalNumberOfPlayers();
        this.numberOfPlayers = numberOfPlayers;
        availableColors = new ArrayList<>(){{
            for(Content content : Content.values()){
                if(content.isResource()){
                    add(content);
                }
            }
        }};
        Collections.shuffle(availableColors);
        players = new ArrayList<>(numberOfPlayers);
        int numberOfVisibleCards = GameParameters.getNumberOfVisibleCards();
        resourceDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(GameParameters.CardType.RESOURCE),
                GameParameters.getStartCardIndex(GameParameters.CardType.RESOURCE),
                numberOfVisibleCards);
        goldDeck = new TurnDeck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(GameParameters.CardType.GOLD),
                GameParameters.getEndCardIndex(GameParameters.CardType.GOLD),
                numberOfVisibleCards);
        starterDeck = new Deck<>(
                CardBuilder::buildCard,
                GameParameters.getStartCardIndex(GameParameters.CardType.STARTER),
                GameParameters.getEndCardIndex(GameParameters.CardType.STARTER));
        objectiveDeck = new Deck<>(
                CardBuilder::buildObjective,
                GameParameters.getStartCardIndex(GameParameters.CardType.OBJECTIVE),
                GameParameters.getEndCardIndex(GameParameters.CardType.OBJECTIVE));
        commonObjectives = new ArrayList<>(){{
            for(int i = 0; i < GameParameters.getNumberOfCommonObjectives(); i++){
                add(objectiveDeck.draw());
            }
        }};
        isLastTurn = false;
    }

    /**
     * obtain all the players playing on this instance of game
     * @return the ArrayList including the players
     */
    public ArrayList<Player> getPlayers(){
        return new ArrayList<>(this.players);
    }

    /**
     * Method that the checks if the maximum number of players is reached
     * @return true if the game is full
     */
    public boolean isGameFull(){
        return players.size() == numberOfPlayers;
    }

    /**
     * Method that checks if there's a user with the same username of the new player that is joining the game
     * @param nickname the nickname to check
     * @return true if there's a duplicate username
     */
    public boolean checkNickname(String nickname){
        return players.stream().anyMatch(p -> p.getNickname().equals(nickname));
    }

    /**
     * Method that gets the remaining colors that a player can choose when entering a game
     * @return the list of colors that the player can choose from
     */
    public ArrayList<Content> getAvailableColors(){
        return new ArrayList<>(availableColors);
    }

    /**
     * Method that adds a player to an existing game
     * @param nickname the nickname of the player
     * @param color player's color
     */
    public void addPlayer(String nickname, Content color) {
        ArrayList<CardSides> handCards = new ArrayList<>(){{
            for(int i = 0; i < GameParameters.getNumberOfGoldCardsInHand(); i++){
                add(goldDeck.draw());
            }
            for(int i = 0; i < GameParameters.getNumberOfResourceCardsInHand(); i++){
                add(resourceDeck.draw());
            }
        }};
        ArrayList<Objective> objectives = new ArrayList<>(){{
            addAll(commonObjectives);
            for(int i = 0; i < GameParameters.getNumberOfSecretObjectives(); i++){
                add(objectiveDeck.draw());
            }
        }};
        players.add(new Player(nickname,color,handCards,objectives));
    }
}