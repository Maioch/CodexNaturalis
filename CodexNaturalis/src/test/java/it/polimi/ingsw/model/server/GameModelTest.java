package it.polimi.ingsw.model.server;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GameModelTest {

    private final int resourceSize = Parameters.getEndCardIndex(CardType.RESOURCE) - Parameters.getStartCardIndex(CardType.RESOURCE) + 1;
    private final int goldSize = Parameters.getEndCardIndex(CardType.GOLD) - Parameters.getStartCardIndex(CardType.GOLD) + 1;
    private final String playerNumberFail = "please set a different number of players for the GameModel instance" +
            " or the tests won't be able to run correctly";

    @Test
    public void isLastTurnTest() throws GameException, GameFullException, NicknameException {
        GameModel gameModel;
        String playerName = "test";
        Content playerContent = Content.RED;
        int players = 2;
        try {
            gameModel = new GameModel(players, new ServerSubject(), 0);
            gameModel.addPlayerData(playerName, playerContent, new TestNetworkHandler());
            gameModel.createPlayers();
            Player player = gameModel.getPlayer(playerName);
            BasicCard starter = CardBuilder.buildCard(81).backSide();
            player.placeStarterCard(starter);
            assertFalse(gameModel.isLastTurn());
            BasicCard previousCard = starter;
            for (int j = 0; j < Parameters.getWinThreshold() + 1; j++) {
                BasicCard card = CardBuilder.buildCard(10).frontSide();
                card.setOwner(player);
                Corner corner = previousCard.getAllCorners().stream()
                        .filter(c -> c.getLocation() == Location.TR)
                        .findFirst().orElseThrow();
                player.placeCard(card, corner);
                previousCard = card;
            }
            assertTrue(gameModel.isLastTurn());
            gameModel = new GameModel(players, new ServerSubject(), 0);
            gameModel.addPlayerData(playerName, playerContent, new TestNetworkHandler());
            gameModel.createPlayers();
            player = gameModel.getPlayer(playerName);
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).getFirst() != null) {
                gameModel.drawCard(player, CardType.RESOURCE, 0);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).size() != 1) {
                gameModel.drawCard(player, CardType.RESOURCE, 1);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.GOLD).getFirst() != null) {
                gameModel.drawCard(player, CardType.GOLD, 0);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.GOLD).size() != 1) {
                gameModel.drawCard(player, CardType.GOLD, 1);
            }
            assertTrue(gameModel.isLastTurn());
            gameModel = new GameModel(players, new ServerSubject(), 0);
            gameModel.addPlayerData(playerName, playerContent, new TestNetworkHandler());
            gameModel.createPlayers();
            player = gameModel.getPlayer(playerName);
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.GOLD).getFirst() != null) {
                gameModel.drawCard(player, CardType.GOLD, 0);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.GOLD).size() != 1) {
                gameModel.drawCard(player, CardType.GOLD, 1);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).getFirst() != null) {
                gameModel.drawCard(player, CardType.RESOURCE, 0);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).size() != 1) {
                gameModel.drawCard(player, CardType.RESOURCE, 1);
            }
            assertTrue(gameModel.isLastTurn());
        } catch (IllegalNumberOfPlayers e) {
            assertTrue(players < Parameters.getMinPlayers() ||
                    players > Parameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    @Test
    public void addPlayerDataTest() throws GameException, NicknameException, IllegalNumberOfPlayers, GameFullException {
        int minPlayers = Parameters.getMinPlayers();
        int maxPlayers = Parameters.getMaxPlayers();
        assertThrows(GameException.class, () -> {
            GameModel wrongGameModel = new GameModel(maxPlayers, new ServerSubject(), 0);
            wrongGameModel.addPlayerData("test", Content.RED, new TestNetworkHandler());
            wrongGameModel.addPlayerData("test2", Content.RED, new TestNetworkHandler());
        });
        assertThrows(NicknameException.class, () -> {
            GameModel wrongGameModel = new GameModel(maxPlayers, new ServerSubject(), 0);
            wrongGameModel.addPlayerData("test", Content.RED, new TestNetworkHandler());
            wrongGameModel.addPlayerData("test", Content.PURPLE, new TestNetworkHandler());
        });
        assertThrows(IllegalNumberOfPlayers.class, () -> new GameModel(maxPlayers + 1, new ServerSubject(), 0));
        for (int numberOfPlayers = minPlayers; numberOfPlayers <= maxPlayers; numberOfPlayers++) {
            ServerSubject serverSubject = new ServerSubject();
            int finalNumberOfPlayers = numberOfPlayers;
            List<TestNetworkHandler> testNetworkHandlers = new ArrayList<>(){{
                for(int i = 0; i < finalNumberOfPlayers; i++){
                    add(new TestNetworkHandler());
                }
            }};
            GameModel gameModel = new GameModel(numberOfPlayers, serverSubject, 0);
            for (int i = 0; i < numberOfPlayers; i++) {
                gameModel.addPlayerData("test"+ i, Content.values()[i], testNetworkHandlers.get(i));
                assertFalse(gameModel.getAvailableColors().contains(Content.values()[i]));
                List<Message> receivedMessages = testNetworkHandlers.get(i).getReceivedMessages();
                assertEquals(i + 2, receivedMessages.size());
                assertEquals(Status.JOIN_GAME, receivedMessages.getFirst().getStatus());
                for(int j = 1; j < i + 2; j++){
                    assertEquals(Status.NEW_PLAYER_JOINED, receivedMessages.get(j).getStatus());
                }
                for(int j = 0; j < i; j++){
                    assertEquals(i + 1, testNetworkHandlers.get(j).getReceivedMessages().size());
                }
            }
            assertThrows(GameFullException.class, () -> gameModel.addPlayerData("TEST", Content.RED, new TestNetworkHandler()));
        }
    }

    @Test
    void createPlayersTest() throws IllegalNumberOfPlayers, GameFullException, NicknameException, GameException {
        GameModel gameModel = new GameModel(2, new ServerSubject(), 0);
        gameModel.addPlayerData("test1", Content.RED, new TestNetworkHandler());
        gameModel.addPlayerData("test2", Content.PURPLE, new TestNetworkHandler());
        assertTrue(gameModel.getAllPlayers().isEmpty());
        gameModel.createPlayers();
        Player player1 = new Player("test1", Content.RED, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        Player player2 = new Player("test2", Content.PURPLE, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        assertEquals(player1, gameModel.getPlayer("test1"));
        assertEquals(player2, gameModel.getPlayer("test2"));
        for(Player player : gameModel.getAllPlayers()){
            assertEquals(gameModel.getCommonObjectives(), player.getObjectives());
            List<CardSides> cards = player.getHandCards();
            int firstCardId = cards.getFirst().frontSide().getCardId();
            assertTrue(firstCardId >= Parameters.getStartCardIndex(CardType.STARTER) &&
                    firstCardId <= Parameters.getEndCardIndex(CardType.STARTER));
            assertEquals(Parameters.getNumberOfResourceCardsInHand(), cards.stream().filter(c -> {
                int cardId = c.frontSide().getCardId();
                return cardId >= Parameters.getStartCardIndex(CardType.RESOURCE) && cardId <=
                        Parameters.getEndCardIndex(CardType.RESOURCE);
            }).toList().size());
            assertEquals(Parameters.getNumberOfGoldCardsInHand(), cards.stream().filter(c -> {
                int cardId = c.frontSide().getCardId();
                return cardId >= Parameters.getStartCardIndex(CardType.GOLD) && cardId <=
                        Parameters.getEndCardIndex(CardType.GOLD);
            }).toList().size());
        }
    }

    @Test
    public void getDrawableCardsTest() throws GameException, GameFullException, NicknameException {
        GameModel gameModel;
        int playersNumber = 2;
        try {
            gameModel = new GameModel(playersNumber, new ServerSubject(), 0);
            gameModel.addPlayerData("resource", Content.GREEN, new TestNetworkHandler());
            gameModel.addPlayerData("gold", Content.RED, new TestNetworkHandler());
            gameModel.createPlayers();
            Player resource = gameModel.getPlayer("resource");
            Player gold = gameModel.getPlayer("gold");
            int currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);

            for(int i = 0; i < resourceSize - 7; i++){
                gameModel.drawCard(resource, CardType.RESOURCE, 0);
                currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
                assertEquals(6, currentlyDrawable);
            }
            for(int i = 0; i < goldSize - 5; i++){
                gameModel.drawCard(gold, CardType.GOLD, 0);
                currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
                assertEquals(6, currentlyDrawable);
            }

            gameModel.drawCard(resource, CardType.RESOURCE, 0);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);
            assertNull(gameModel.getDrawableCards().get(CardType.RESOURCE).getFirst());
            gameModel.drawCard(gold, CardType.GOLD, 0);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);
            assertNull(gameModel.getDrawableCards().get(CardType.GOLD).getFirst());
            gameModel.drawCard(resource, CardType.RESOURCE, 1);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(5, currentlyDrawable);
            gameModel.drawCard(gold, CardType.GOLD, 1);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(4, currentlyDrawable);
            gameModel.drawCard(resource, CardType.RESOURCE, 1);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(3, currentlyDrawable);
            gameModel.drawCard(gold, CardType.GOLD, 1);
            currentlyDrawable = gameModel.getDrawableCards().get(CardType.RESOURCE).size() + gameModel.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(2, currentlyDrawable);
            assertTrue(gameModel.isLastTurn());
        } catch (IllegalNumberOfPlayers e) {
            assertTrue(playersNumber < Parameters.getMinPlayers() ||
                    playersNumber > Parameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    @Test
    public void drawCardTest() throws GameException{
        String nickname = "test";
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nickname, handler1);
        serverSubject.subscribe("test2", handler2);

        int numberOfPlayers = 2;
        try {
            GameModel gameModel = new GameModel(numberOfPlayers, serverSubject, 0);
            Player playerTest = new Player(nickname, Content.RED, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
            int index = 0;
            gameModel.drawCard(playerTest, CardType.RESOURCE, index);

            List<Message> received = handler1.getReceivedMessages();
            assertEquals(1, received.size());
            assertEquals(Status.DRAW_OPTIONS, received.removeFirst().getStatus());
            received = handler2.getReceivedMessages();
            assertEquals(1, received.size());
            assertEquals(Status.DRAW_OPTIONS, received.removeFirst().getStatus());

            assertFalse(playerTest.getHandCards().isEmpty());
            gameModel.drawCard(playerTest, CardType.GOLD, index);
            assertEquals(playerTest.getHandCards().size(), 2);
            index = 1;
            gameModel.drawCard(playerTest, CardType.RESOURCE, index);
            assertEquals(playerTest.getHandCards().size(), 3);
            gameModel.drawCard(playerTest, CardType.GOLD, index);
            assertEquals(playerTest.getHandCards().size(), 4);
        }
        catch (IllegalNumberOfPlayers e){
            assertTrue(numberOfPlayers < Parameters.getMinPlayers() ||
                    numberOfPlayers > Parameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    @Test
    public void getWinningPlayersTest() throws GameException, GameFullException, NicknameException {
        int numberOfPlayers = 2;
        try{
            GameModel gameModel = new GameModel(numberOfPlayers, new ServerSubject(), 0);
            ArrayList<String> nicknames = new ArrayList<>(Arrays.asList("test", "test2"));

            assertTrue(gameModel.getWinningPlayers().isEmpty());

            gameModel.addPlayerData(nicknames.getFirst(), Content.RED, new TestNetworkHandler());
            gameModel.addPlayerData(nicknames.get(1), Content.BLUE, new TestNetworkHandler());
            gameModel.createPlayers();

            Corner fakeCorner = new Corner(Content.WHITE, Location.TR);

            gameModel.getPlayer(nicknames.getFirst()).placeCard(CardBuilder.buildCard(9).frontSide(), fakeCorner);

            assertEquals(nicknames.getFirst(), gameModel.getWinningPlayers().getFirst());

            gameModel.getPlayer(nicknames.get(1)).placeCard(CardBuilder.buildCard(9).frontSide(), fakeCorner);

            assertEquals(nicknames.stream().sorted().toList(), gameModel.getWinningPlayers().stream().sorted().toList());

        }catch (IllegalNumberOfPlayers e){
            assertTrue(numberOfPlayers < Parameters.getMinPlayers() ||
                    numberOfPlayers > Parameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    @Test
    void isLobbyEmptyTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = Parameters.getMinPlayers();
        int maxPlayers = Parameters.getMaxPlayers();
        assertThrows(IllegalNumberOfPlayers.class, () -> new GameModel(minPlayers - 1, new ServerSubject(), 1));
        assertThrows(IllegalNumberOfPlayers.class, () -> new GameModel(maxPlayers + 1, new ServerSubject(), 1));
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertTrue(game.isLobbyEmpty());
        game.addPlayerData("test", Content.RED, new TestNetworkHandler());
        assertFalse(game.isLobbyEmpty());
    }

    @Test
    void getNumberOfPlayersTest() throws IllegalNumberOfPlayers{
        int minPlayers = Parameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertEquals(minPlayers, game.getNumberOfPlayers());
    }

    @Test
    void getLobbyNicknamesTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = Parameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertEquals(new ArrayList<>(), game.getLobbyNicknames());
        game.addPlayerData("test", Content.RED, new TestNetworkHandler());
        assertEquals(new ArrayList<>(List.of("test")), game.getLobbyNicknames());
        game.addPlayerData("test1", Content.BLUE, new TestNetworkHandler());
        assertEquals(new ArrayList<>(Arrays.asList("test", "test1")), game.getLobbyNicknames());
    }

    @Test
    void checkNicknameTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = Parameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        game.addPlayerData("test", Content.RED, new TestNetworkHandler());
        assertThrows(NicknameException.class,
                () -> game.addPlayerData("te st", Content.RED, new TestNetworkHandler()));
        assertThrows(NicknameException.class,
                () -> game.addPlayerData(String.format("test%s", Parameters.getDelimiter()), Content.RED, new TestNetworkHandler()));
        assertThrows(NicknameException.class,
                () -> game.addPlayerData(String.format("test%s", Parameters.getCommandChar()), Content.RED, new TestNetworkHandler()));
        assertThrows(NicknameException.class,
                () -> game.addPlayerData("t".repeat(Parameters.getMaxNameLength() + 1), Content.RED, new TestNetworkHandler()));
        assertThrows(NicknameException.class,
                () -> game.addPlayerData("test", Content.RED, new TestNetworkHandler()));
    }

    @Test
    void deletePlayerDataTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException {
        String nickname = "test";
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe("test2", handler2);

        int minPlayers = Parameters.getMinPlayers();
        GameModel gameModel = new GameModel(minPlayers, serverSubject, 0);
        gameModel.addPlayerData(nickname, Content.RED, handler1);
        handler1.getReceivedMessages();
        handler2.getReceivedMessages();
        gameModel.deletePlayerData(nickname);

        List<Message> received = handler1.getReceivedMessages();
        assertEquals(1, received.size());
        assertEquals(Status.PLAYER_LEFT_LOBBY, received.removeFirst().getStatus());
        received = handler2.getReceivedMessages();
        assertEquals(1, received.size());
        assertEquals(Status.PLAYER_LEFT_LOBBY, received.removeFirst().getStatus());

        gameModel.deletePlayerData("test2");

        received = handler1.getReceivedMessages();
        assertEquals(0, received.size());
        received = handler2.getReceivedMessages();
        assertEquals(0, received.size());

        assertFalse(gameModel.getLobbyNicknames().contains(nickname));
        assertTrue(gameModel.getAvailableColors().contains(Content.RED));
    }

    @Test
    void drawObjectiveCards() throws IllegalNumberOfPlayers {
        GameModel game = new GameModel(Parameters.getMinPlayers(), new ServerSubject(), 0);
        int startCardIndex = Parameters.getStartCardIndex(CardType.OBJECTIVE);
        int endCardIndex = Parameters.getEndCardIndex(CardType.OBJECTIVE);
        int numberOfObjectives = endCardIndex - startCardIndex;
        for(int i = 0; i < numberOfObjectives / Parameters.getNumberOfDrawnSecretObjectives(); i++){
            game.drawObjectiveCards();
        }
        assertThrows(DeckException.class, game::drawObjectiveCards);
    }
    
    @Test
    void isGameStuckTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException{
        int minPlayers = Parameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 0);
        assertTrue(game.isGameStuck());
        game.addPlayerData("test", Content.RED, new TestNetworkHandler());
        game.addPlayerData("test1", Content.BLUE, new TestNetworkHandler());
        game.createPlayers();
        assertTrue(game.isGameStuck());
        Player player = game.getPlayer("test");
        player.placeStarterCard(CardBuilder.buildCard(Parameters.getStartCardIndex(CardType.STARTER)).backSide());
        assertFalse(game.isGameStuck());
    }

    @Test
    void getCommonObjectivesTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException {
        int minPlayers = Parameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 0);
        assertNotNull(game.getCommonObjectives());
        assertFalse(game.getCommonObjectives().isEmpty());
        game.addPlayerData("test", Content.GREEN, new TestNetworkHandler());
        game.addPlayerData("test1", Content.PURPLE, new TestNetworkHandler());
        game.createPlayers();
        assertEquals(game.getCommonObjectives(), game.getPlayer("test").getObjectives());
        assertEquals(game.getCommonObjectives(), game.getPlayer("test1").getObjectives());
    }

    @Test
    void exceptionDrawCardTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException{
        GameModel game = new GameModel(2, new ServerSubject(), 1);
        game.addPlayerData("test", Content.RED, new TestNetworkHandler());
        game.addPlayerData("test1", Content.BLUE, new TestNetworkHandler());
        game.createPlayers();
        try{
            game.drawCard(game.getPlayer("test"), CardType.STARTER, 0);
        } catch (GameException e){
            assertEquals(e.getMessage(), "The given deck type is invalid");
        }
        try{
            game.drawCard(game.getPlayer("test"), CardType.RESOURCE, -1);
        } catch (GameException e){
            assertEquals(e.getMessage(), "Invalid draw index");
        }
        try{
            game.drawCard(game.getPlayer("test"), CardType.RESOURCE, Parameters.getNumberOfVisibleCards() + 1);
        } catch (GameException e){
            assertEquals(e.getMessage(), "Invalid draw index");
        }
        try{
            while (game.getDrawableCards().get(CardType.RESOURCE).getFirst() != null) {
                game.drawCard(game.getPlayer("test"), CardType.RESOURCE, 0);
            }
            game.drawCard(game.getPlayer("test"), CardType.RESOURCE, 0);
        } catch (GameException e){
            assertEquals(e.getMessage(), "The given deck is empty");
        }
    }
}