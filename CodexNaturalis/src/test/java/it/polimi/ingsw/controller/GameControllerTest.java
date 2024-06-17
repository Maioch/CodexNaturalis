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

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class GameControllerTest {
    private final int waitDurationMilliSeconds = 100;
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
        handlers.getFirst().awaitForMessage(Status.JOIN_GAME, waitDurationMilliSeconds, List.of(Status.NEW_PLAYER_JOINED));
        assertEquals(serverSubject.getNetworkHandler(nickname1),handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_NICKNAME, waitDurationMilliSeconds);
        assertEquals(serverSubject.getNetworkHandler(nickname1),handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.RED, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_COLOR, waitDurationMilliSeconds);
        assertNull(serverSubject.getNetworkHandler(nickname2));

        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.GREEN, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME, waitDurationMilliSeconds, List.of(Status.NEW_PLAYER_JOINED,
                Status.TURN_NOTIFICATION, Status.DRAW_OPTIONS));
        assertEquals(serverSubject.getNetworkHandler(nickname3), handlers.get(1));

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.GAME_FULL, waitDurationMilliSeconds);
        assertNull(serverSubject.getNetworkHandler(nickname2));
    }

    @Test
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
        Thread.sleep(waitDurationMilliSeconds);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
    }

    @Test
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
        Thread.sleep(GameParameters.getPingPeriodSeconds() * 2000L);
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.PLAYER_DISCONNECTED, waitDurationMilliSeconds);
        }
        testStarterCardPhase(handlers);
        testObjectivesPhase(handlers);
        boolean isLastTurn = false;
        boolean isDeckEmpty = false;
        while(!isLastTurn){
            for(TestNetworkHandler handler : handlers){
                testPlaceCardPhase(handler, handlers);
                if(!isDeckEmpty) {
                    isDeckEmpty = testDrawCardPhase(handler);
                }
            }
            checkTurnSkipped(handlers);
            for(TestNetworkHandler handler : handlers){
                isLastTurn = handler.removeIfStatus(Status.LAST_TURN, waitDurationMilliSeconds);
            }
        }
        handlers.add(new TestNetworkHandler(game));
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test5"));
        handlers.getLast().awaitForMessage(Status.WRONG_NAME, waitDurationMilliSeconds);
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test3"));
        List<Status> expectedStatus = List.of(Status.JOIN_GAME, Status.NEW_PLAYER_JOINED, Status.DRAW_OPTIONS,
                Status.SILENT_TURN_NOTIFICATION, Status.PLACEMENT_OK, Status.PLAYER_HAND_CARDS, Status.COMMON_OBJECTIVES,
                Status.SECRET_OBJECTIVES, Status.PLAYER_HAND_BACK);
        for(Status status : expectedStatus) {
            handlers.getLast().removeIfStatus(status, waitDurationMilliSeconds);
        }
        for(TestNetworkHandler handler : handlers) {
            assertTrue(handler.removeIfStatus(Status.RECONNECT, waitDurationMilliSeconds));
        }
        for(TestNetworkHandler handler : handlers){
            testPlaceCardPhase(handler, handlers);
        }
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.DECLARE_WINNER, waitDurationMilliSeconds);
        }
        for(TestNetworkHandler handler : handlers) {
            assertNull(handler.getCurrentGame());
        }
        for(int i = 0; i < numPlayers; i++) {
            assertNull(serverSubject.getNetworkHandler("test" + i));
        }
        assertTrue(isGameEnded.get());
    }

    private void testStarterCardPhase(List<TestNetworkHandler> handlers) {
        boolean isFirstRound = true;
        for(TestNetworkHandler starterHandler : handlers) {
            Message message = starterHandler.awaitForMessage(Status.STARTER_CARD, waitDurationMilliSeconds);
            for (TestNetworkHandler handler : handlers) {
                handler.awaitForMessage(Status.TURN_NOTIFICATION, waitDurationMilliSeconds);
                if(isFirstRound) {
                    handler.awaitForMessage(Status.DRAW_OPTIONS, waitDurationMilliSeconds);
                    isFirstRound = false;
                }
            }
            assertFalse(starterHandler.removeIfStatus(Status.TURN_SKIPPED, waitDurationMilliSeconds));
            assertInstanceOf(CardHandMessage.class, message);
            BasicCard starterCard = ((CardHandMessage) message).getCardHand().getFirst().backSide();
            starterHandler.send(new CardPlacementMessage(starterCard, null));
        }
        checkTurnSkipped(handlers);
    }

    private void testObjectivesPhase(List<TestNetworkHandler> handlers) {
        for(TestNetworkHandler objectiveHandler : handlers) {
            Message message = objectiveHandler.awaitForMessage(Status.REQUEST_SECRET_OBJECTIVES, waitDurationMilliSeconds);
            objectiveHandler.awaitForMessage(Status.COMMON_OBJECTIVES, waitDurationMilliSeconds);
            for (TestNetworkHandler handler : handlers) {
                handler.awaitForMessage(Status.TURN_NOTIFICATION, waitDurationMilliSeconds);
            }
            assertFalse(objectiveHandler.removeIfStatus(Status.TURN_SKIPPED, waitDurationMilliSeconds));
            assertInstanceOf(ObjectivesMessage.class, message);
            Objective objective = ((ObjectivesMessage) message).getObjectives().getFirst();
            objectiveHandler.send(new ObjectivesMessage(Status.REQUEST_SECRET_OBJECTIVES, List.of(objective)));
        }
        checkTurnSkipped(handlers);
    }

    private void testPlaceCardPhase(TestNetworkHandler handler, List<TestNetworkHandler> handlers) {
        Message message = handler.awaitForMessage(Status.PLACE_CARD, waitDurationMilliSeconds);
        for(TestNetworkHandler currentHandler : handlers) {
            currentHandler.awaitForMessage(Status.TURN_NOTIFICATION, waitDurationMilliSeconds);
        }
        assertInstanceOf(ValidPlacementsMessage.class, message);
        BasicCard cardToPlace = ((ValidPlacementsMessage) message).getPlaceableCards().getLast();
        Corner corner = ((ValidPlacementsMessage) message).getPlaceableCorners().getFirst();
        handler.send(new CardPlacementMessage(cardToPlace, corner));
    }

    private boolean testDrawCardPhase(TestNetworkHandler handler) {
        Message message = handler.awaitForMessage(Status.DRAW, waitDurationMilliSeconds);
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
            assertTrue(handler.removeIfStatus(Status.TURN_SKIPPED, waitDurationMilliSeconds));
        }
    }
}