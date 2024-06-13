package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {

    @Test
    void getNameTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        for(int i = 1; i < 6; i++) {
            manager.addGame(2, "testGame" + i);
            GameController controller = manager.getController(i);
            List<GameInfo> gameInfo = manager.getFormattedAvailableMatches();
            assertEquals(controller.getName(), gameInfo.get(i-1).getGameName());
        }
    }

    @Test
    void getGameStatusTest() throws IllegalNumberOfPlayers {
        GamesManager manager = new GamesManager();
        for(int i = 1; i < 6; i++) {
            manager.addGame(2, "testGame" + i);
            GameController controller = manager.getController(i);
            List<GameInfo> gameInfo = manager.getFormattedAvailableMatches();
            assertEquals(controller.getGameStatus(), gameInfo.get(i-1).getGameStatus());
        }
    }
}
