package it.polimi.ingsw.controller.server;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.core.Server;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.shared.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.shared.messages.setup.NewGameMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.logging.Handler;

import static org.junit.jupiter.api.Assertions.*;

public class ServerMessageHandlerTest {
    @Test
    @Timeout(value = 20, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
    void runTest() throws InterruptedException {
        GamesManager gamesManager = new GamesManager();
        ServerMessageHandler handler = new ServerMessageHandler(gamesManager);
        new Thread(handler).start();
        TestNetworkHandler user = new TestNetworkHandler();
        Message response;

        handler.addEventToQueue(new LabeledMessage(user,
                new NewGameMessage("test", GameParameters.getMaxPlayers() + 1)));
        response = user.awaitForMessage(Status.INVALID_PLAYERS_NUMBER);
        assertNull(gamesManager.getController(1));

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.NEW_GAME)));
        handler.addEventToQueue(new LabeledMessage(user, new NewGameMessage("test",2)));
        response = user.awaitForMessage(Status.NEW_GAME);
        assertNotNull(gamesManager.getController(1));
        assertEquals("test",gamesManager.getController(1).getName());
        assertInstanceOf(IntegerMessage.class, response);
        assertEquals(1, ((IntegerMessage) response).getValue());

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.SILENT_TURN_NOTIFICATION)));

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.REQUEST_GAMES)));
        response = user.awaitForMessage(Status.REQUEST_GAMES);
        assertInstanceOf(MatchListMessage.class, response);
        assertEquals((gamesManager.getFormattedAvailableMatches()),((MatchListMessage)response).getMatchList());

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.REQUEST_COLORS)));
        handler.addEventToQueue(new LabeledMessage(user, new IntegerMessage(Status.REQUEST_COLORS, 2)));
        user.awaitForMessage(Status.ERROR);
        handler.addEventToQueue(new LabeledMessage(user, new IntegerMessage(Status.REQUEST_COLORS, 1)));
        user.awaitForMessage(Status.REQUEST_COLORS);

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.RECONNECT)));
        handler.addEventToQueue(new LabeledMessage(user,
                new JoinGameMessage(Status.RECONNECT, "test", null, null, 2)));
        user.awaitForMessage(Status.INVALID_RECONNECT);
        handler.addEventToQueue(new LabeledMessage(user,
                new JoinGameMessage(Status.RECONNECT, "test", null, null, 1)));

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.REQUEST_PING)));
        user.awaitForMessage(Status.PING_ACK);

        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.JOIN_GAME)));
        handler.addEventToQueue(new LabeledMessage(user,
                new JoinGameMessage(Status.JOIN_GAME, "test", Content.BLUE, null, 2)));
        user.awaitForMessage(Status.ERROR);
        handler.addEventToQueue(new LabeledMessage(user,
                new JoinGameMessage(Status.JOIN_GAME, "test", Content.BLUE, null, 1)));
        user.awaitForMessage(Status.JOIN_GAME);
        Thread.sleep(500);

        assertEquals(gamesManager.getController(1), user.getCurrentGame());
        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.PING_ACK)));
        handler.addEventToQueue(new LabeledMessage(user, new Message(Status.PLAYER_DISCONNECTED)));
        user.awaitForMessage(Status.PLAYER_LEFT_LOBBY, List.of(Status.NEW_PLAYER_JOINED));
        Thread.sleep(500);
        assertNull(user.getCurrentGame());
    }
}
