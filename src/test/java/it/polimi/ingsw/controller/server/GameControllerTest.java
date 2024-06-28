package it.polimi.ingsw.controller.server;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.server.ExchangeHandlerManager;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.*;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

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
        assertEquals("⏳ lobby", gameStatus.getText());
    }

    @Test
    void equalsTest() throws IllegalNumberOfPlayers {
        GameController controller = new GameController(2, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY), (gameController) -> {});
        assertEquals(new GameController(3, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY), (gameController) -> {}), controller);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(controller, "test");
    }

    @Test
    void lobbyDeletionTest() throws IllegalNumberOfPlayers, InterruptedException{
        AtomicBoolean isOk = new AtomicBoolean(false);
        new Thread(new GameController(2, new ServerSubject(),
                new GameInfo(1, "test", GameStatus.LOBBY),
                (g) -> isOk.set(true))).start();
        Thread.sleep(Parameters.getLobbyTimeout() * 1000L + 1000L);
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
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();

        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getFirst().awaitForMessage(Status.JOIN_GAME);
        handlers.getFirst().awaitForMessage(Status.NEW_PLAYER_JOINED);
        assertEquals(serverSubject.getExchangeHandler(nickname1), handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_NICKNAME);
        assertEquals(serverSubject.getExchangeHandler(nickname1), handlers.getFirst());

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.RED, null, gameId));
        handlers.getLast().awaitForMessage(Status.INVALID_COLOR);
        assertNull(serverSubject.getExchangeHandler(nickname2));

        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.GREEN, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME);
        assertEquals(serverSubject.getExchangeHandler(nickname3), handlers.get(1));

        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getLast().awaitForMessage(Status.GAME_FULL);
        assertNull(serverSubject.getExchangeHandler(nickname2));
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void lobbyTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        String nickname3 = "test3";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();

        GameController game = new GameController(3, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        new Thread(game).start();
        for (TestNetworkHandler handler : handlers) {
            handlerManager.addHandler(handler);
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

        handlers.set(1, new TestNetworkHandler(game, handlerManager));
        handlers.get(1).send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.get(1).awaitForMessage(Status.JOIN_GAME);
        assertEquals(GameStatus.LOBBY, game.getGameStatus());

        handlers.get(2).send(new JoinGameMessage(Status.JOIN_GAME, nickname3, Content.PURPLE, null, gameId));
        Thread.sleep(100);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void autoDrawTest() throws IllegalNumberOfPlayers, InterruptedException{
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();

        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        for (TestNetworkHandler handler : handlers) {
            handlerManager.addHandler(handler);
        }
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME,nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME,nickname2, Content.BLUE, null, gameId));
        handlers = getPlayerInOrder(handlers, 2);
        testStarterCardPhase(handlers);
        testObjectivesPhase(handlers);
        testPlaceCardPhase(handlers.getFirst());
        handlers.getFirst().stop();
        Thread.sleep(100);
        handlers.getLast().getReceivedMessages();
        handlers.getLast().awaitForMessage(Status.PLAYER_DISCONNECTED);
        handlers.getLast().awaitForMessage(Status.DRAW_OPTIONS, List.of(Status.PLAYER_HAND_BACK));
        handlers.getLast().stop();
    }

    @Test
    void deadGameTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();
        AtomicBoolean gameEnded = new AtomicBoolean(false);
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> gameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        for (TestNetworkHandler handler : handlers) {
            handlerManager.addHandler(handler);
        }
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.getFirst().stop();
        handlers.getLast().stop();
        Thread.sleep(Parameters.getServerPingPeriodSeconds() * 2500L);
        assertTrue(gameEnded.get());
    }

    @Test
    @Timeout(value = 120, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void winByForfeitTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();
        AtomicBoolean gameEnded = new AtomicBoolean(false);
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> gameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        for (TestNetworkHandler handler : handlers) {
            handlerManager.addHandler(handler);
        }
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers = getPlayerInOrder(handlers, 2);
        handlers.getFirst().stop();
        Thread.sleep(Parameters.getServerPingPeriodSeconds() * 2000L);
        handlers.getLast().removeStatus(Status.DECLARE_WINNER);
        Thread.sleep(100);
        assertTrue(gameEnded.get());
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void wakeUpAfterReconnectTest() throws IllegalNumberOfPlayers, InterruptedException {
        String nickname1 = "test1";
        String nickname2 = "test2";
        int gameId = 1;
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();
        GameController game = new GameController(2, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY), (g) -> {});
        List<TestNetworkHandler> handlers = new ArrayList<>() {{
            add(new TestNetworkHandler(game, handlerManager));
            add(new TestNetworkHandler(game, handlerManager));
        }};
        for (TestNetworkHandler handler : handlers) {
            handlerManager.addHandler(handler);
        }
        new Thread(game).start();
        handlers.getFirst().send(new JoinGameMessage(Status.JOIN_GAME, nickname1, Content.RED, null, gameId));
        handlers.getLast().send(new JoinGameMessage(Status.JOIN_GAME, nickname2, Content.BLUE, null, gameId));
        handlers.removeFirst().stop();
        handlers.getFirst().removeStatus(Status.PLAYER_DISCONNECTED);
        Thread.sleep(100);
        assertEquals(game.getGameStatus(), GameStatus.PLAYER_DISCONNECTED);
        handlers.add(new TestNetworkHandler(game, handlerManager));
        handlerManager.addHandler(handlers.getLast());
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test1"));
        game.wakeUpAfterReconnect();
        for(TestNetworkHandler handler : handlers) {
            handler.removeStatus(Status.RECONNECT);
        }
        Thread.sleep(Parameters.getServerPingPeriodSeconds() * 2000L);
        assertEquals(GameStatus.STARTED, game.getGameStatus());
    }

    @Test
    @Timeout(value = 60, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void fullMatchTest() throws IllegalNumberOfPlayers, InterruptedException {
        int gameId = 1;
        int numPlayers = 4;
        AtomicBoolean isGameEnded = new AtomicBoolean(false);
        ServerSubject serverSubject = new ServerSubject();
        ExchangeHandlerManager handlerManager = new ExchangeHandlerManager();
        GameController game = new GameController(numPlayers, serverSubject,
                new GameInfo(gameId, "test", GameStatus.LOBBY),
                (g) -> isGameEnded.set(true));
        List<TestNetworkHandler> handlers = new ArrayList<>();
        List<TestNetworkHandler> finalHandlers = handlers;
        Map<TestNetworkHandler, String> playersNames = new HashMap<>(){{
            for(int i = 0; i < numPlayers; i++){
                TestNetworkHandler handler = new TestNetworkHandler(game, handlerManager);
                finalHandlers.add(handler);
                handlerManager.addHandler(handler);
                put(handler, "test" + i);
            }
        }};
        new Thread(game).start();
        for(int i = 0; i < numPlayers; i++) {
            handlers.get(i).send(new JoinGameMessage(Status.JOIN_GAME, playersNames.get(handlers.get(i)),
                    Content.values()[i], null, gameId));
        }
        handlers = getPlayerInOrder(handlers, numPlayers);
        TestNetworkHandler disconnectedHandler = handlers.removeLast();
        disconnectedHandler.stop();
        for(TestNetworkHandler handler : handlers) {
            handler.removeStatus(Status.PLAYER_DISCONNECTED);
        }
        testStarterCardPhase(handlers);
        checkTurnSkipped(handlers);
        for(TestNetworkHandler handler : handlers) {
            handler.awaitForMessage(Status.TURN_NOTIFICATION, List.of(Status.PLACEMENT_OK, Status.PLAYER_HAND_BACK, Status.PLAYER_HAND_CARDS));
        }
        testObjectivesPhase(handlers);
        checkTurnSkipped(handlers);
        TestNetworkHandler sender = handlers.get(1);
        handlers.get(1).send(new ChatMessage("TEST", null, playersNames.entrySet().stream()
                .filter(e -> e.getKey() != sender).map(Map.Entry::getValue).toList()));
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
        handlers.add(new TestNetworkHandler(game, handlerManager));
        handlers.getLast().send(new StringMessage(Status.RECONNECT, "test5"));
        handlers.getLast().awaitForMessage(Status.WRONG_NAME);
        handlers.getLast().send(new StringMessage(Status.RECONNECT, playersNames.get(disconnectedHandler)));
        handlerManager.addHandler(handlers.getLast());
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
            assertNull(serverSubject.getExchangeHandler("test" + i));
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
    }

    private void testObjectivesPhase(List<TestNetworkHandler> handlers) {
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

    private List<TestNetworkHandler> getPlayerInOrder(List<TestNetworkHandler> handlers, int numPlayers) throws InterruptedException{
        Thread.sleep(100);
        TestNetworkHandler[] orderedHandlers = new TestNetworkHandler[numPlayers];
        List<Integer> turnPosition = new ArrayList<>();
        for(int i = 0; i < numPlayers; i++) {
            JoinGameMessage message = (JoinGameMessage)handlers.getLast()
                    .awaitForMessage(Status.NEW_PLAYER_JOINED, List.of(Status.JOIN_GAME));
            int position = message.getGameInfo();
            turnPosition.add(position);
        }
        for(int i = 0; i < numPlayers; i++) {
            orderedHandlers[turnPosition.get(i) - 1] = handlers.get(i);
        }
        return Arrays.stream(orderedHandlers).collect(Collectors.toList());
    }
}