package it.polimi.ingsw.controller;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class GameInfoTest {

    @Test
    void gettersTest(){
        GameInfo gameInfo = new GameInfo(1, "test", GameStatus.LOBBY);
        assertEquals(1, gameInfo.getGameId());
        assertEquals("test", gameInfo.getGameName());
        assertEquals(GameStatus.LOBBY, gameInfo.getGameStatus());
    }

    @Test
    void settersTest(){
        GameInfo gameInfo = new GameInfo(1, "test", GameStatus.LOBBY);
        gameInfo.setGameStatus(GameStatus.PLAYER_DISCONNECTED);
        assertEquals(GameStatus.PLAYER_DISCONNECTED, gameInfo.getGameStatus());
        gameInfo.setGameStatus(GameStatus.STARTED);
        assertEquals(GameStatus.STARTED, gameInfo.getGameStatus());
    }

    @Test
    void equalsTest(){
        GameInfo gameInfo = new GameInfo(1, "test", GameStatus.STARTED);
        assertEquals(new GameInfo(1, "test", GameStatus.PLAYER_DISCONNECTED), gameInfo);
        assertNotEquals(new GameInfo(2, "test", GameStatus.LOBBY), gameInfo);
        assertNotEquals(new GameInfo(1, "test1", GameStatus.LOBBY), gameInfo);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals("test", gameInfo);
    }
}