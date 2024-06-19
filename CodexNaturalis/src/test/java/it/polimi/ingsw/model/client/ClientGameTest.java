package it.polimi.ingsw.model.client;

import it.polimi.ingsw.TestSubmitter;
import it.polimi.ingsw.TestView;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static it.polimi.ingsw.TestView.*;

public class ClientGameTest {

    @Test
    void isGameFullTest(){
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), new TestView(), 3, 1);
        assertFalse(game.isGameFull());
        game.addRemotePlayer(new RemotePlayer("test", Content.RED));
        game.addRemotePlayer(new RemotePlayer("test", Content.RED));
        assertTrue(game.isGameFull());
    }

    @Test
    void getNumberOfPlayersTest(){
        assertEquals(2, new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), new TestView(), 2, 1).getNumberOfPlayers());
    }

    @Test
    void getGameIdTest(){
        assertEquals(1, new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), new TestView(), 2, 1).getGameId());
    }

    @Test
    void getLocalPlayerTest(){
        assertEquals(new LocalPlayer("test", Content.RED), new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), new TestView(), 2, 1).getLocalPlayer());
    }

    @Test
    void getRemotePlayersTest(){
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), new TestView(), 2, 1);
        List<RemotePlayer> remotePlayers = List.of(
                new RemotePlayer("test1", Content.RED),
                new RemotePlayer("test2", Content.RED));
        game.addRemotePlayer(remotePlayers.getFirst());
        game.addRemotePlayer(remotePlayers.getLast());
        assertEquals(remotePlayers, game.getRemotePlayers());
    }

    @Test
    void getPlayerWithNicknameTest(){
        List<ClientPlayer> players = List.of(
                new LocalPlayer("test", Content.RED),
                new RemotePlayer("test1", Content.RED),
                new RemotePlayer("test2", Content.RED));
        ClientGame game = new ClientGame((LocalPlayer) players.getFirst(), new TestSubmitter(), new TestView(), 2, 1);
        game.addRemotePlayer((RemotePlayer) players.get(1));
        game.addRemotePlayer((RemotePlayer) players.get(2));
        assertEquals(players.getFirst(), game.getPlayerWithNickname("test"));
        assertEquals(players.get(1), game.getPlayerWithNickname("test1"));
        assertEquals(players.get(2), game.getPlayerWithNickname("test2"));
    }

    @Test
    void getPlayerColorsTest(){
        Map<String, Content> expected = new HashMap<>(){{
            put("test", Content.RED);
            put("test1", Content.BLUE);
            put("test2", Content.GREEN);
        }};
        List<ClientPlayer> players = List.of(
                new LocalPlayer("test", Content.RED),
                new RemotePlayer("test1", Content.BLUE),
                new RemotePlayer("test2", Content.GREEN));
        ClientGame game = new ClientGame((LocalPlayer) players.getFirst(), new TestSubmitter(), new TestView(), 2, 1);
        game.addRemotePlayer((RemotePlayer) players.get(1));
        game.addRemotePlayer((RemotePlayer) players.get(2));
        assertEquals(expected, game.getPlayerColors());
    }

    @Test
    void drawTest(){
        TestView testView = new TestView();
        Map<CardType, List<BasicCard>> arg1 = new HashMap<>(){{
            put(CardType.RESOURCE, List.of(CardBuilder.buildCard(1).frontSide(), CardBuilder.buildCard(2).frontSide()));
            put(CardType.GOLD, List.of(CardBuilder.buildCard(41).frontSide()));
        }};
        Map<CardType, Integer> arg2 = new HashMap<>(){{
            put(CardType.RESOURCE, 5);
            put(CardType.GOLD, 6);
        }};
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), testView, 2, 1);
        game.setDrawableOptions(arg1, arg2);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "updateDecks", List.of(arg1, arg2));
        game.requestDraw(arg1, arg2);
        recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "requestDraw", List.of(arg1, arg2));
    }

    @Test
    void playerWithTurnTest(){
        String local = "test";
        String remote1 = "test1";
        String remote2 = "test2";
        String nonExistent = "test3";
        TestView testView = new TestView();
        List<ClientPlayer> players = List.of(
                new LocalPlayer(local, Content.RED),
                new RemotePlayer(remote1, Content.RED),
                new RemotePlayer(remote2, Content.RED));
        ClientGame game = new ClientGame((LocalPlayer) players.getFirst(), new TestSubmitter(), testView, 2, 1);
        game.addRemotePlayer((RemotePlayer) players.get(1));
        game.addRemotePlayer((RemotePlayer) players.get(2));
        testView.getRecentCalls();

        game.setPlayerWithTurn(nonExistent, true);
        var recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.size(),1);
        checkForUpdate(recentCalls, "turnChanged", List.of(local));
        assertEquals(local, game.getPlayerWithTurn().getNickname());

        game.setPlayerWithTurn(remote1, true);
        recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.size(),1);
        checkForUpdate(recentCalls, "turnChanged", List.of(remote1));
        assertEquals(remote1, game.getPlayerWithTurn().getNickname());

        game.setPlayerWithTurn(local, true);
        recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.size(),1);
        checkForUpdate(recentCalls, "turnChanged", List.of(local));
        assertEquals(local, game.getPlayerWithTurn().getNickname());

        game.setPlayerWithTurn(remote1, false);
        recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.size(),0);
    }

    @Test
    void addRemotePlayerTest(){
        TestView testView = new TestView();
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), testView, 2, 1);
        RemotePlayer remotePlayer = new RemotePlayer("test1", Content.BLUE);
        game.addRemotePlayer(remotePlayer);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "showUserJoined", List.of("test1", Content.BLUE, game.isGameFull()));
    }

    @Test
    void removeRemotePlayerTest(){
        TestView testView = new TestView();
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), testView, 2, 1);
        RemotePlayer remotePlayer = new RemotePlayer("test1", Content.BLUE);
        game.addRemotePlayer(remotePlayer);
        testView.getRecentCalls();
        game.removeRemotePlayer("test1");
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "notifyPlayerLeftLobby", List.of("test1", Content.BLUE));
        game.removeRemotePlayer("test1");
        recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "notifyPlayerLeftLobby", List.of("test1", Content.WHITE));
    }

    @Test
    void commonObjectivesTest(){
        TestView testView = new TestView();
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), testView, 2, 1);
        assertTrue(game.getCommonObjectives().isEmpty());
        List<Objective> objectivesList = List.of(
                CardBuilder.buildObjective(90),
                CardBuilder.buildObjective(91),
                CardBuilder.buildObjective(92));
        game.setCommonObjectives(objectivesList);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "showCommonObjectives", List.of(objectivesList));
        assertEquals(objectivesList, game.getCommonObjectives());
    }
}