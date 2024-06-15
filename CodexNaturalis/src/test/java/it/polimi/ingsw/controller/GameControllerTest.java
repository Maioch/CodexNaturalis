package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {
    @Test
    void getNameTest() throws IllegalNumberOfPlayers {
        GameController gameController = new GameController(
                2,
                new ServerSubject(),
                new GameInfo(1,"test", GameStatus.LOBBY),
                (a) -> {});
        assertEquals("test", gameController.getName());
    }

    @Test
    void getGameStatusTest() throws IllegalNumberOfPlayers {
        GameStatus gameStatus = GameStatus.LOBBY;
        GameController gameController = new GameController(
                2,
                new ServerSubject(),
                new GameInfo(1,"test", gameStatus),
                (a) -> {});
        assertEquals(gameStatus, gameController.getGameStatus());
    }

    @Test
    void equalsTest() throws IllegalNumberOfPlayers {
        GameController controller = new GameController(2, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY), (gameController) -> {});
        assertEquals(new GameController(3, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY), (gameController) -> {}), controller);
    }
}