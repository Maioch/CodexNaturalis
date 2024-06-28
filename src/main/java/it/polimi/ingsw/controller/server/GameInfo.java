package it.polimi.ingsw.controller.server;

import java.io.Serializable;

/**
 * Contains general game's information.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
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
     * Gets the id assigned to the game when it was first created.
     *
     * @return the game's id.
     */
    public int getGameId(){
        return gameId;
    }

    /**
     * Gets the name chosen by the game's creator.
     *
     * @return the game's name.
     */
    public String getGameName(){
        return gameName;
    }

    /**
     * Gets the current state of the game.
     *
     * @return the game's state.
     */
    public GameStatus getGameStatus(){
        return gameStatus;
    }

    /**
     * Sets the game's state when it changes.
     *
     * @param gameStatus the new game's state.
     */
    public void setGameStatus(GameStatus gameStatus) {
        this.gameStatus = gameStatus;
    }

    /**
     * Equals method.
     *
     * @param object object to check.
     *
     * @return       true if each immutable field is equals to the corresponding field of object.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof GameInfo other){
            return gameId == other.gameId && gameName.equals(other.gameName);
        }
        return false;
    }
}