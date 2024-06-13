package it.polimi.ingsw.model.client;

import it.polimi.ingsw.TestView;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        Map<Method, List<Object>> recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        List<Object> updateContent = recentCalls.get(getMethod("updateDecks", Map.class, Map.class));
        assertEquals(List.of(arg1, arg2), updateContent);
        game.requestDraw(arg1, arg2);
        recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        updateContent = recentCalls.get(getMethod("requestDraw", Map.class, Map.class));
        assertEquals(List.of(arg1, arg2), updateContent);
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

        game.setPlayerWithTurn(nonExistent);
        Map<Method, List<Object>> recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.entrySet().size(),1);
        List<Object> updateContent = recentCalls.get(getMethod("turnChanged", String.class));
        assertEquals(local, updateContent.getFirst());
        assertEquals(local, game.getPlayerWithTurn().getNickname());

        game.setPlayerWithTurn(remote1);
        recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.entrySet().size(),1);
        updateContent = recentCalls.get(getMethod("turnChanged", String.class));
        assertEquals(remote1, updateContent.getFirst());
        assertEquals(remote1, game.getPlayerWithTurn().getNickname());

        game.setPlayerWithTurn(local);
        recentCalls = testView.getRecentCalls();
        assertEquals(recentCalls.entrySet().size(),1);
        updateContent = recentCalls.get(getMethod("turnChanged", String.class));
        assertEquals(local, updateContent.getFirst());
        assertEquals(local, game.getPlayerWithTurn().getNickname());

        assertEquals(players.getFirst(), game.getPlayerWithNickname("test"));
        assertEquals(players.get(1), game.getPlayerWithNickname("test1"));
        assertEquals(players.get(2), game.getPlayerWithNickname("test2"));
    }

    @Test
    void addRemotePlayerTest(){
        TestView testView = new TestView();
        ClientGame game = new ClientGame(new LocalPlayer("test", Content.RED),
                new TestSubmitter(), testView, 2, 1);
        RemotePlayer remotePlayer = new RemotePlayer("test1", Content.BLUE);
        game.addRemotePlayer(remotePlayer);
        Map<Method, List<Object>> recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        List<Object> updateContent = recentCalls.get(getMethod("showUserJoined", String.class, Content.class, boolean.class));
        assertEquals(List.of("test1", Content.BLUE, game.isGameFull()), updateContent);
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
        Map<Method, List<Object>> recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        List<Object> updateContent = recentCalls.get(getMethod("notifyPlayerLeftLobby", String.class, Content.class));
        assertEquals(List.of("test1", Content.BLUE), updateContent);
        game.removeRemotePlayer("test1");
        recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        updateContent = recentCalls.get(getMethod("notifyPlayerLeftLobby", String.class, Content.class));
        assertEquals(List.of("test1", Content.WHITE), updateContent);
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
        Map<Method, List<Object>> recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.entrySet().size());
        List<Object> updateContent = recentCalls.get(getMethod("showCommonObjectives", List.class));
        assertEquals(objectivesList, updateContent.getFirst());
        assertEquals(objectivesList, game.getCommonObjectives());
    }

    private Method getMethod(String name, Class<?>... parameters) {
        try {
            return TestView.class.getDeclaredMethod(name, parameters);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static class TestSubmitter implements EventSubmitter{
        public void submit(Runnable runnable){
            runnable.run();
        }
    }
}