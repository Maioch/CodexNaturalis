package it.polimi.ingsw.controller.client;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.TestSubmitter;
import it.polimi.ingsw.TestView;
import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.controller.server.GameStatus;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.*;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.GameColorsMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.shared.messages.setup.MatchListMessage;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.TestView.checkForUpdate;
import static org.junit.jupiter.api.Assertions.*;

public class ClientControllerTest {

    @Test
    void sendMessageTest() {
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        controller.setNetworkHandler(networkHandler);
        controller.sendMessage(new Message(Status.CHAT));
        assertEquals(Status.CHAT, networkHandler.getReceivedMessages().getFirst().getStatus());
    }

    @Test
    void setGameViewTest() throws InterruptedException{
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        new Thread(controller).start();
        controller.addEventToQueue(new LabeledMessage(networkHandler,
                new JoinGameMessage(Status.JOIN_GAME, "test", Content.RED, 2, 1)));
        Thread.sleep(1000);
        controller.setGameView(new TestView());
        Thread.sleep(1000);
        assertEquals(1, controller.getGameId());
        controller.stop();
    }

    @Test
    void gettersTest() throws InterruptedException{
        String nick1 = "test";
        String nick2 = "test1";
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        new Thread(controller).start();
        controller.setGameView(new TestView());
        controller.addEventToQueue(new LabeledMessage(networkHandler, new Message(Status.CHAT)));
        assertEquals(controller.getEventFromQueue().message().getStatus(), Status.CHAT);
        controller.addEventToQueue(new LabeledMessage(networkHandler,
                new JoinGameMessage(Status.JOIN_GAME, nick1, Content.RED, 2, 1)));
        controller.addEventToQueue(new LabeledMessage(networkHandler,
                new JoinGameMessage(Status.NEW_PLAYER_JOINED, nick1, Content.RED, 1, 1)));
        controller.addEventToQueue(new LabeledMessage(networkHandler,
                new JoinGameMessage(Status.NEW_PLAYER_JOINED, nick2, Content.BLUE, 2, 1)));
        Thread.sleep(1000);
        assertEquals(nick1, controller.getLocalPlayerName());
        assertEquals(List.of(nick2), controller.getRemotePlayerNames());
        assertTrue(controller.getLocalPlayerBoard().isEmpty());
        assertTrue(controller.getRemotePlayerBoard(nick2).isEmpty());
        assertTrue(controller.getLocalPlayerValidCards().isEmpty());
        assertTrue(controller.getLocalPlayerValidCorners().isEmpty());
        assertEquals(Map.of(nick1,Content.RED, nick2,Content.BLUE), controller.getPlayerColors());
        assertTrue(controller.getCommonObjectives().isEmpty());
        assertTrue(controller.getPersonalObjectives().isEmpty());
        assertTrue(controller.getLocalPlayerHand().isEmpty());
        assertTrue(controller.getRemotePlayerHand(nick2).isEmpty());
        controller.stop();
    }

    @Test
    void runSetupTest() throws InterruptedException{
        TestView testView = new TestView();
        ClientController controller = new ClientController(testView, new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler(controller);
        controller.setNetworkHandler(networkHandler);
        Map<Message, Pair<String, List<Object>>> setupMessagesTest = Map.ofEntries(
                Map.entry(new MatchListMessage(Status.REQUEST_GAMES, List.of(new GameInfo(1, "test", GameStatus.LOBBY))),
                        new Pair<>("updateMatchList", List.of(List.of(new GameInfo(1, "test", GameStatus.LOBBY))))),
                Map.entry(new IntegerMessage(Status.NEW_GAME, 1),
                        new Pair<>("newGameSuccess", List.of(1))),
                Map.entry(new Message(Status.INVALID_PLAYERS_NUMBER),
                        new Pair<>("showCriticalError", List.of(Status.INVALID_PLAYERS_NUMBER.getMessage()))),
                Map.entry(new GameColorsMessage(Status.REQUEST_COLORS, List.of(Content.RED), 1),
                        new Pair<>("showJoinGameDialog", List.of(List.of(Content.RED), 1))),
                Map.entry(new GameColorsMessage(Status.REQUEST_COLORS, new ArrayList<>(), 1),
                        new Pair<>("showCriticalError", List.of(Status.GAME_FULL.getMessage()))),
                Map.entry(new Message(Status.GAME_FULL),
                        new Pair<>("showCriticalError", List.of(Status.GAME_FULL.getMessage()))),
                Map.entry(new IntegerMessage(Status.INVALID_NICKNAME, 1),
                        new Pair<>("showUserError", List.of(Status.INVALID_NICKNAME.getMessage(), 1))),
                Map.entry(new IntegerMessage(Status.INVALID_COLOR, 1),
                        new Pair<>("showUserError", List.of(Status.INVALID_COLOR.getMessage(), 1))),
                Map.entry(new Message(Status.WRONG_NAME),
                        new Pair<>("showCriticalError", List.of(Status.WRONG_NAME.getMessage()))),
                Map.entry(new Message(Status.ERROR),
                        new Pair<>("showCriticalError", List.of("The lobby for this match timed out. Please create a new one."))),
                Map.entry(new Message(Status.INVALID_RECONNECT),
                        new Pair<>("showReconnectionError", List.of(Status.INVALID_RECONNECT.getMessage())))
        );
        new Thread(controller).start();
        for(var entry : setupMessagesTest.entrySet()){
            controller.addEventToQueue(new LabeledMessage(networkHandler, entry.getKey()));
            Thread.sleep(500);
            var recentCalls = testView.getRecentCalls();
            assertEquals(1, recentCalls.size());
            checkForUpdate(recentCalls, entry.getValue().getKey(), entry.getValue().getValue());
        }
        controller.stop();
    }

    @Test
    void runGameTest() throws InterruptedException{
        String nick1 = "test";
        String nick2 = "test1";
        Content color1 = Content.RED;
        Content color2 = Content.BLUE;
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        controller.setNetworkHandler(networkHandler);
        List<Message> gameMessages = List.of(
                new JoinGameMessage(Status.JOIN_GAME, nick1, Content.RED, 2, 1),
                new JoinGameMessage(Status.NEW_PLAYER_JOINED, nick1, color1, 1, 1),
                new JoinGameMessage(Status.NEW_PLAYER_JOINED, nick2, color2, 2, 1),
                new JoinGameMessage(Status.NEW_PLAYER_JOINED, nick2, color2, 2, 1),
                new StringMessage(Status.TURN_NOTIFICATION, nick1),
                new StringMessage(Status.SILENT_TURN_NOTIFICATION, nick1),
                new DrawOptionsMessage(Status.DRAW_OPTIONS, new HashMap<>(), new HashMap<>()),
                new Message(Status.INVALID_STARTER_CARD),
                new CardHandMessage(Status.STARTER_CARD, new ArrayList<>()),
                new ObjectivesMessage(Status.COMMON_OBJECTIVES, new ArrayList<>()),
                new Message(Status.INVALID_SECRET_OBJECTIVES),
                new ObjectivesMessage(Status.SECRET_OBJECTIVES, new ArrayList<>()),
                new ObjectivesMessage(Status.SECRET_OBJECTIVES, new ArrayList<>()),
                new Message(Status.INVALID_PLACE_CARD),
                new ValidPlacementsMessage(Status.PLACE_CARD, new ArrayList<>(), new ArrayList<>()),
                new Message(Status.NO_MOVES),
                new PlayerBoardMessage(new ArrayList<>(), 0),
                new CardHandMessage(Status.PLAYER_HAND_CARDS,new ArrayList<>()),
                new CardHandMessage(Status.PLAYER_HAND_BACK,new ArrayList<>()),
                new Message(Status.INVALID_DRAW),
                new DrawOptionsMessage(Status.DRAW, new HashMap<>(), new HashMap<>()),
                new Message(Status.LAST_TURN),
                new PlayerSummaryMessage(new HashMap<>(), 0, nick1),
                new Message(Status.GAME_TIMEOUT_STARTED),
                new WinnersMessage(new ArrayList<>()),
                new Message(Status.GAME_CANCELED),
                new Message(Status.TURN_SKIPPED),
                new StringMessage(Status.PLAYER_DISCONNECTED, nick2),
                new StringMessage(Status.PLAYER_LEFT_LOBBY, nick2),
                new StringMessage(Status.RECONNECT, nick2),
                new ChatMessage(nick1,nick2, new ArrayList<>()),
                new Message(Status.REQUEST_PING)
        );
        for(Message message : gameMessages){
            controller.addEventToQueue(new LabeledMessage(networkHandler, message));
        }
        new Thread(controller).start();
        Thread.sleep(2000);
        controller.stop();
    }

    @Test
    void backToSetupTest() throws InterruptedException{
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        new Thread(controller).start();
        controller.setGameView(new TestView());
        controller.addEventToQueue(new LabeledMessage(networkHandler,
                new JoinGameMessage(Status.JOIN_GAME, "test", Content.RED, 2, 1)));
        Thread.sleep(1000);
        assertEquals(1, controller.getGameId());
        controller.backToSetup();
        assertThrows(NullPointerException.class, controller::getGameId);
        controller.stop();
    }

    @Test
    void stopTest() throws InterruptedException{
        ClientController controller = new ClientController(new TestView(), new TestSubmitter());
        Thread toBeStopped = new Thread(controller);
        toBeStopped.start();
        Thread.sleep(100);
        assertFalse(toBeStopped.isInterrupted());
        controller.stop();
        Thread.sleep(100);
        assertTrue(toBeStopped.isInterrupted());
    }
    
    @Test
    void handlePingsTest() throws InterruptedException{
        TestView testView = new TestView();
        ClientController controller = new ClientController(testView, new TestSubmitter());
        TestNetworkHandler networkHandler = new TestNetworkHandler();
        networkHandler.stop();
        controller.setNetworkHandler(networkHandler);
        new Thread(controller).start();
        Thread.sleep(GameParameters.getPingPeriodSeconds() * 2500L);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "showDisconnectionMessage", new ArrayList<>());
        controller.stop();
    }
}