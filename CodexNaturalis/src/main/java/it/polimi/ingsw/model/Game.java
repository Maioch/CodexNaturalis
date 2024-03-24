package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.*;

import java.util.ArrayList;

/**
 * Class that represents a single match of Codex Naturalis
 * @author Guglielmo Gatti
 */
public class Game {
    public final int resourceCardStartIndex = 0;
    public final int goldCardStartIndex = 20;
    public final int starterCardStartIndex = 40;
    public final int objectiveCardStartIndex = 60;
    public final int deckEndIndex = 80;
    public final int numberOfVisibleCards = 2;
    public final ArrayList<Player> players;
    public final ArrayList<String> nicknames;
    public final TurnDeck resourceDeck;
    public final TurnDeck goldDeck;
    public final Deck starterDeck;
    public final Deck objectivesDeck;
    public final boolean isLastTurn;
    public final int currentPlayer;

    public Game(){
        players = new ArrayList<Player>();
        nicknames = new ArrayList<String>();
        resourceDeck = new TurnDeck(resourceCardStartIndex, goldCardStartIndex - 1, numberOfVisibleCards);
        goldDeck = new TurnDeck(goldCardStartIndex, starterCardStartIndex - 1, numberOfVisibleCards);
        starterDeck = new Deck(starterCardStartIndex, objectiveCardStartIndex - 1);
        objectivesDeck = new Deck(objectiveCardStartIndex, deckEndIndex);
        isLastTurn = false;
        currentPlayer = 0;
    }

    /**
     * Method that represents the main game loop
     * @return the status with which the game has ended
     */
    public boolean playGame(){
        return true;
    }

    /**
     * obtain all the players playing on this instance of game
     * @return the ArrayList including the players
     */
    public ArrayList<Player> getPlayers(){
        return (ArrayList<Player>) players.clone();
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
    private boolean initializeGame(){
        return true;
    }

}
