package it.polimi.ingsw.controller;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.*;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
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
    @Timeout(value = 5, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void acceptPlayerTest() throws IllegalNumberOfPlayers {
        String nickname1 = "test1";
        String nickname2 = "test2";
        String nickname3 = "test3";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();

        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getFirst().awaitForMessage(Status.JOIN_GAME);
        handlers.getFirst().awaitForMessage(Status.NEW_PLAYER_JOINED);
        assertEquals(serverSubject.getNetworkHandler(nickname1), handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_NICKNAME);
        assertEquals(serverSubject.getNetworkHandler(nickname1), handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.RED, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_COLOR);
        assertNull(serverSubject.getNetworkHandler(nickname2));

        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.GREEN, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME);
        assertEquals(serverSubject.getNetworkHandler(nickname3), handlers.get(1));

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.GAME_FULL);
        assertNull(serverSubject.getNetworkHandler(nickname2));
    }

    @Test
    @Timeout(value = 15, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void lobbyTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        String nickname3 = "test3";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();

        GameController game = new GameController(3, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        for (TestNetworkHandler handler : handlers) {
            handler.send(new Message(Status.REQUEST_COLORS));
            handler.awaitForMessage(Status.REQUEST_COLORS);
        }

        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getFirst().awaitForMessage(Status.JOIN_GAME);
        handlers.getFirst().awaitForMessage(Status.NEW_PLAYER_JOINED);

        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.get(1).stop();
        handlers.getFirst().awaitForMessage(Status.PLAYER_LEFT_LOBBY, List.of(Status.NEW_PLAYER_JOINED));

        handlers.get(2).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.PURPLE, null, gameId));
        handlers.get(2).awaitForMessage(Status.JOIN_GAME);
        handlers.get(2).awaitForMessage(Status.NEW_PLAYER_JOINED);
        handlers.get(2).awaitForMessage(Status.NEW_PLAYER_JOINED);
        assertEquals(GameStatus.LOBBY, game.getGameStatus());

        handlers.get(2).send(new Message(Status.PLAYER_DISCONNECTED));
        handlers.getFirst().awaitForMessage(Status.PLAYER_LEFT_LOBBY, List.of(Status.NEW_PLAYER_JOINED));

        handlers.set(1, new TestNetworkHandler(game));
        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME);
        assertEquals(GameStatus.LOBBY, game.getGameStatus());

        handlers.get(2).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.PURPLE, null, gameId));
        Thread.sleep(100);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
    }

    @Test
    void autoDrawTest() throws IllegalNumberOfPlayers {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();

        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME,nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME,nickname2, Content.BLUE, null, gameId));
        /*testStarterCardPhase(handlers);
        testObjectivesPhase(handlers);*/
    }

    @Test
    void deadGameTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        AtomicBoolean gameEnded = new AtomicBoolean(false);
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> gameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getFirst().stop();
        handlers.getLast().stop();
        Thread.sleep(GameParameters.getPingPeriodSeconds() * 2500L);
        assertTrue(gameEnded.get());
    }

    @Test
    @Timeout(value = 120, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void winByForfeitTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        AtomicBoolean gameEnded = new AtomicBoolean(false);
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> gameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getFirst().stop();
        handlers.getLast().removeStatus(Status.DECLARE_WINNER);
        Thread.sleep(100);
        assertTrue(gameEnded.get());
    }

    @Test
    @Timeout(value = 70, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void wakeUpAfterReconnectTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game));
            add(new TestNetworkHandler(game));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.removeLast().stop();
        handlers.getFirst().removeStatus(Status.PLAYER_DISCONNECTED);
        Thread.sleep(100);
        assertEquals(game.getGameStatus(), GameStatus.PLAYER_DISCONNECTED);
        handlers.add(new TestNetworkHandler(game));
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test2"));
        for(TestNetworkHandler handler : handlers) {
            handler.removeStatus(Status.RECONNECT);
        }
        Thread.sleep(GameParameters.getPingPeriodSeconds() * 2000L);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
    }

    @Test
    @Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void fullMatchTest() throws IllegalNumberOfPlayers, InterruptedException {
        int gameId = 1;
        int numPlayers = 4;
        AtomicBoolean isGameEnded = new AtomicBoolean(false);
        ServerSubject serverSubject = new ServerSubject();
        GameController game = new GameController(numPlayers, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY),
                (g) -> isGameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>(){{
            for(int i = 0; i < numPlayers; i++){
                add(new TestNetworkHandler(game));
            }
        }};
        new Thread(game).start();
        for(int i = 0; i < numPlayers; i++) {
            handlers.get(i).send(new JoinGameMessage(Status.JOIN_GAME,"test" + i, Content.values()[i], null, gameId));
        }
        handlers.removeLast().stop();
        for(TestNetworkHandler handler : handlers) {
            handler.removeStatus(Status.PLAYER_DISCONNECTED);
        }
        testStarterCardPhase(handlers);
        testObjectivesPhase(handlers);
        handlers.get(1).send(new ChatMessage("TEST", null, List.of("test0", "test2", "test3")));
        for(TestNetworkHandler handler : handlers) {
            handler.removeStatus(Status.CHAT);
        }
        boolean isLastTurn = false;
        boolean isDeckEmpty = false;
        while(!isLastTurn){
            for(TestNetworkHandler currentHandler : handlers) {
                currentHandler.awaitForMessage(Status.TURN_NOTIFICATION);
            }
            for(TestNetworkHandler handler : handlers){
                testPlaceCardPhase(handler);
                if(!isDeckEmpty) {
                    isDeckEmpty = testDrawCardPhase(handler);
                }
                for(TestNetworkHandler currentHandler : handlers) {
                    currentHandler.awaitForMessage(Status.TURN_NOTIFICATION,
                            List.of(Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.PLAYER_HAND_BACK, Status.DRAW_OPTIONS));
                }
            }
            checkTurnSkipped(handlers);
            for(TestNetworkHandler handler : handlers){
                isLastTurn = handler.containsStatus(Status.LAST_TURN, 100);
            }
        }
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.LAST_TURN);
        }
        for(TestNetworkHandler currentHandler : handlers) {
            currentHandler.awaitForMessage(Status.TURN_NOTIFICATION);
        }
        handlers.add(new TestNetworkHandler(game));
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test5"));
        handlers.getLast().awaitForMessage(Status.WRONG_NAME);
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test3"));
        List<Status> expectedStatus = List.of(Status.JOIN_GAME, Status.NEW_PLAYER_JOINED, Status.DRAW_OPTIONS,
                Status.SILENT_TURN_NOTIFICATION, Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.COMMON_OBJECTIVES,
                Status.SECRET_OBJECTIVES, Status.PLAYER_HAND_BACK, Status.TURN_NOTIFICATION);
        for(Status currentStatus : expectedStatus) {
            handlers.getLast().removeStatus(currentStatus);
        }
        handlers.getLast().awaitForMessage(Status.RECONNECT, expectedStatus);
        for(TestNetworkHandler handler : handlers.subList(0, handlers.size() - 1)) {
            handler.removeStatus(Status.RECONNECT);
        }
        for(TestNetworkHandler handler : handlers){
            testPlaceCardPhase(handler);
            if(handler != handlers.getLast()) {
                for (TestNetworkHandler currentHandler : handlers) {
                    currentHandler.awaitForMessage(Status.TURN_NOTIFICATION,
                            List.of(Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.PLAYER_HAND_BACK));
                }
            }
        }
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.DECLARE_WINNER,
                    List.of(Status.PLAYER_FINAL_SCORE, Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.PLAYER_HAND_BACK));
        }
        Thread.sleep(100);
        for(TestNetworkHandler handler : handlers) {
            assertNull(handler.getCurrentGame());
        }
        for(int i = 0; i < numPlayers; i++) {
            assertNull(serverSubject.getNetworkHandler("test" + i));
        }
        assertTrue(isGameEnded.get());
    }

    private void testStarterCardPhase(List<TestNetworkHandler> handlers) {
        for (TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.DRAW_OPTIONS, List.of(Status.JOIN_GAME, Status.NEW_PLAYER_JOINED));
            handler.awaitForMessage(Status.TURN_NOTIFICATION);
        }
        for(TestNetworkHandler starterHandler : handlers) {
            Message message = starterHandler.awaitForMessage(Status.STARTER_CARD);
            assertInstanceOf(CardHandMessage.class, message);
            BasicCard starterCard = ((CardHandMessage) message).getCardHand().getFirst().backSide();
            starterHandler.send(new CardPlacementMessage(starterCard, null));
            for(TestNetworkHandler handler : handlers) {
                handler.awaitForMessage(Status.TURN_NOTIFICATION, List.of(Status.PLACEMENT_OK, Status.PLAYER_HAND_BACK, Status.PLAYER_HAND_CARDS));
            }
        }
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.TURN_SKIPPED);
            handler.awaitForMessage(Status.PLAYER_HAND_BACK, List.of(Status.PLACEMENT_OK));
        }
    }

    private void testObjectivesPhase(List<TestNetworkHandler> handlers) {
        for (TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.TURN_NOTIFICATION);
        }
        for(TestNetworkHandler objectiveHandler : handlers) {
            objectiveHandler.awaitForMessage(Status.COMMON_OBJECTIVES);
            Message message = objectiveHandler.awaitForMessage(Status.REQUEST_SECRET_OBJECTIVES);
            assertInstanceOf(ObjectivesMessage.class, message);
            Objective objective = ((ObjectivesMessage) message).getObjectives().getFirst();
            objectiveHandler.send(new ObjectivesMessage(Status.REQUEST_SECRET_OBJECTIVES, List.of(objective)));
            for (TestNetworkHandler handler : handlers) {
                handler.awaitForMessage(Status.TURN_NOTIFICATION, List.of(Status.SECRET_OBJECTIVES));
            }
        }
        checkTurnSkipped(handlers);
    }

    private void testPlaceCardPhase(TestNetworkHandler handler) {
        Message message = handler.awaitForMessage(Status.PLACE_CARD);
        assertInstanceOf(ValidPlacementsMessage.class, message);
        BasicCard cardToPlace = ((ValidPlacementsMessage) message).getPlaceableCards().getLast();
        Corner corner = ((ValidPlacementsMessage) message).getPlaceableCorners().getFirst();
        handler.send(new CardPlacementMessage(cardToPlace, corner));
    }

    private boolean testDrawCardPhase(TestNetworkHandler handler) {
        Message message = handler.awaitForMessage(Status.DRAW,
                List.of(Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.PLAYER_HAND_BACK));
        assertInstanceOf(DrawOptionsMessage.class, message);
        Map<CardType,List<BasicCard>> drawableOptions = ((DrawOptionsMessage) message).getDrawableOptions();
        CardType typeChosen = CardType.RESOURCE;
        int indexChosen = 0;
        for(Map.Entry<CardType, List<BasicCard>> entry : drawableOptions.entrySet()){
            boolean found = false;
            for(int i = 0; i < entry.getValue().size(); i++){
                BasicCard card = entry.getValue().get(i);
                if(card != null){
                    typeChosen = entry.getKey();
                    indexChosen = i;
                    found = true;
                    break;
                }
            }
            if(found){
                break;
            }
        }
        handler.send(new DrawChoiceMessage(indexChosen, typeChosen));
        return ((DrawOptionsMessage) message).getDrawableOptions().values()
                .stream().flatMap(Collection::stream).toList().size() == 3;
    }

    private void checkTurnSkipped(List<TestNetworkHandler> handlers) {
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.TURN_SKIPPED);
        }
    }
}