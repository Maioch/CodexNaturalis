package it.polimi.ingsw.controller;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    void lobbyDeletionTest() throws IllegalNumberOfPlayers, InterruptedException{
        AtomicBoolean isOk = new AtomicBoolean(false);
        new Thread(new GameController(2, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY),
                (g) -> isOk.set(true))).start();
        Thread.sleep(GameParameters.getLobbyTimeout() * 1000L + 1000L);
        assertTrue(isOk.get());
    }

    @Test
    void fullMatchTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        String nickname3 = "test3";
        int gameId = 1;
        int waitDurationMilliSeconds = 500;
        ServerSubject serverSubject = new ServerSubject();

        GameController game = new GameController(3, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY),
                (g) -> serverSubject.notifyAll(new Message(Status.DECLARE_WINNER)));
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        for (TestNetworkHandler handler : handlers) {
            handler.send(new Message(Status.REQUEST_COLORS));
            handler.awaitForMessage(Status.REQUEST_COLORS, waitDurationMilliSeconds);
        }

        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getFirst().awaitForMessage(Status.NEW_PLAYER_JOINED, waitDurationMilliSeconds);
        handlers.getFirst().awaitForMessage(Status.JOIN_GAME, waitDurationMilliSeconds);

        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.get(1).stop();
        Thread.sleep(GameParameters.getPingPeriodSeconds() * 2000L);
        handlers.getFirst().awaitForMessage(Status.PLAYER_LEFT_LOBBY, waitDurationMilliSeconds, List.of(Status.NEW_PLAYER_JOINED));

        handlers.get(2).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.PURPLE, null, gameId));
        handlers.get(2).awaitForMessage(Status.NEW_PLAYER_JOINED, waitDurationMilliSeconds);
        handlers.get(2).awaitForMessage(Status.NEW_PLAYER_JOINED, waitDurationMilliSeconds);
        handlers.get(2).awaitForMessage(Status.JOIN_GAME, waitDurationMilliSeconds);
        assertEquals(GameStatus.LOBBY, game.getGameStatus());

        handlers.get(2).send(new Message(Status.PLAYER_DISCONNECTED));
        handlers.getFirst().awaitForMessage(Status.PLAYER_LEFT_LOBBY, waitDurationMilliSeconds, List.of(Status.NEW_PLAYER_JOINED));

        handlers.set(1, new TestNetworkHandler(game));
        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME, waitDurationMilliSeconds, List.of(Status.NEW_PLAYER_JOINED));
        assertEquals(GameStatus.LOBBY, game.getGameStatus());

        handlers.get(2).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.PURPLE, null, gameId));
        Thread.sleep(waitDurationMilliSeconds * 5);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
        for(TestNetworkHandler starterHandler : handlers) {
            starterHandler.awaitForMessage(Status.STARTER_CARD, waitDurationMilliSeconds);
            for (TestNetworkHandler handler : handlers) {
                handler.awaitForMessage(Status.TURN_NOTIFICATION, waitDurationMilliSeconds);
                handler.awaitForMessage(Status.DRAW_OPTIONS, waitDurationMilliSeconds);
            }
        }
    }
}