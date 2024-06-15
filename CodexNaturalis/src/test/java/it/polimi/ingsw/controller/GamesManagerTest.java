package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GamesManagerTest {

    @Test
    void getControllerTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        assertNull(manager.getController(1));
        manager.addGame(2, "test");
        assertEquals(new GameController(2, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY), (controller)->{}),
                manager.getController(1));
    }

    @Test
    void addGameTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        manager.addGame(2, "test");
        assertEquals(new GameController(2, new ServerSubject(),
                        new GameInfo(1, "test", GameStatus.LOBBY), (controller)->{}),
                manager.getController(1));
        manager.addGame(2, "test");
        assertEquals(new GameController(2, new ServerSubject(),
                        new GameInfo(2, "test", GameStatus.LOBBY), (controller)->{}),
                manager.getController(2));
        manager.addGame(2, "test");
        assertEquals(new GameController(2, new ServerSubject(),
                        new GameInfo(3, "test", GameStatus.LOBBY), (controller)->{}),
                manager.getController(3));
        manager.deleteGame(manager.getController(2));
        manager.addGame(2, "test");
        assertEquals(new GameController(2, new ServerSubject(),
                        new GameInfo(2, "test", GameStatus.LOBBY), (controller)->{}),
                manager.getController(2));
    }

    @Test
    void deleteGameTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        manager.addGame(2, "test");
        manager.deleteGame(manager.getController(1));
        assertNull(manager.getController(1));
    }

    @Test
    void getFormattedAvailableMatchesTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        manager.addGame(2, "test");
        manager.addGame(2, "test");
        manager.addGame(2, "test");
        List<GameInfo> expectedResult = List.of(
                new GameInfo(1, "test", GameStatus.LOBBY),
                new GameInfo(2, "test", GameStatus.LOBBY),
                new GameInfo(3, "test", GameStatus.LOBBY)
        );
        assertEquals(expectedResult, manager.getFormattedAvailableMatches());
    }
}