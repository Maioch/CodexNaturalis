package it.polimi.ingsw.controller;

import java.io.Serializable;

/**
 * GameInfo is a class used to associate a game with its related information.
 */
public class GameInfo implements Serializable {

    private final int gameId;
    private final String gameName;
    private GameStatus gameStatus;

    /**
     * Class constructor.
     *
     * @param gameId     the game's id.
     * @param gameName   the game's name.
     * @param gameStatus the game's status.
     *
     * @see GameStatus
     */
    public GameInfo(int gameId, String gameName, GameStatus gameStatus) {
        this.gameId = gameId;
        this.gameName = gameName;
        this.gameStatus = gameStatus;
    }

    /**
     * Returns the id assigned to the game when it was first created.
     *
     * @return the game's id.
     */
    public int getGameId(){
        return gameId;
    }

    /**
     * Returns the name chosen by the game's creator.
     *
     * @return the game's name.
     */
    public String getGameName(){
        return gameName;
    }

    /**
     * Returns the current state of the game.
     *
     * @return the game's state.
     */
    public GameStatus getGameStatus(){
        return gameStatus;
    }

    /**
     * Updates the game's state when it changes.
     *
     * @param gameStatus the new game's state.
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }
}