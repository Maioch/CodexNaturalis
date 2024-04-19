package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.*;

import java.util.ArrayList;

/**
 * Class that represents a single match of Codex Naturalis
 * @author Guglielmo Gatti
 */
public class Game {
    public final ArrayList<Player> players;
    public final ArrayList<String> nicknames;
    public final TurnDeck resourceDeck;
    public final TurnDeck goldDeck;
    public final Deck starterDeck;
    public final Deck objectiveDeck;
    public final boolean isLastTurn;
    public final int currentPlayer;

    public Game(){
        players = new ArrayList<>();
        nicknames = new ArrayList<>();
        int numberOfVisibleCards = GameParameters.getNumberOfVisibleCards();
        resourceDeck = new TurnDeck(
                GameParameters.getStartCardIndex(GameParameters.CardType.RESOURCE),
                GameParameters.getEndCardIndex(GameParameters.CardType.RESOURCE),
                numberOfVisibleCards);
        goldDeck = new TurnDeck(
                GameParameters.getStartCardIndex(GameParameters.CardType.GOLD),
                GameParameters.getEndCardIndex(GameParameters.CardType.GOLD),
                numberOfVisibleCards);
        starterDeck = new Deck(
                GameParameters.getStartCardIndex(GameParameters.CardType.STARTER),
                GameParameters.getEndCardIndex(GameParameters.CardType.STARTER));
        objectiveDeck = new Deck(
                GameParameters.getStartCardIndex(GameParameters.CardType.OBJECTIVE),
                GameParameters.getEndCardIndex(GameParameters.CardType.OBJECTIVE));
        isLastTurn = false;
        currentPlayer = 0;
    }

    /**
     * obtain all the players playing on this instance of game
     * @return the ArrayList including the players
     */
    public ArrayList<Player> getPlayers(){
        return new ArrayList<>(this.players);
    }

    /**
     * obtain the game's winner
     * @return the name of the winner
     */
    public String getWinner(){
        return "no winner";
    }

    /**
     * initializes the game
     * @return the initialization status
     */
    public boolean initializeGame(){
        return true;
    }
}