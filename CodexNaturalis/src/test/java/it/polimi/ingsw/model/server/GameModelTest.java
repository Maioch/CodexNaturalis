package it.polimi.ingsw.model.server;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guglielmo Gatti
 */
public class GameModelTest {

    private final int resourceSize = GameParameters.getEndCardIndex(CardType.RESOURCE) - GameParameters.getStartCardIndex(CardType.RESOURCE) + 1;
    private final int goldSize = GameParameters.getEndCardIndex(CardType.GOLD) - GameParameters.getStartCardIndex(CardType.GOLD) + 1;
    private final String playerNumberFail = "please set a different number of players for the GameModel instance" +
            " or the tests won't be able to run correctly";

    /**
     * Tests whether the game class can correctly assess whether a game should end during the next turn or not
     */
    @Test
    public void isLastTurnTest() throws GameException, GameFullException, NicknameException {
        GameModel gameModel;
        String playerName = "test";
        Content playerContent = Content.RED;
        int players = 2;
        try {
            gameModel = new GameModel(players, new ServerSubject(), 0);
            gameModel.addPlayerData(playerName, playerContent);
            gameModel.createPlayers();
            Player player = gameModel.getPlayer(playerName);
            BasicCard starter = CardBuilder.buildCard(81).backSide();
            player.placeStarterCard(starter);
            assertFalse(gameModel.isLastTurn());
            BasicCard previousCard = starter;
            for (int j = 0; j < GameParameters.getWinThreshold() + 1; j++) {
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
            gameModel.addPlayerData(playerName, playerContent);
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
            gameModel.addPlayerData(playerName, playerContent);
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
            assertTrue(players < GameParameters.getMinPlayers() ||
                    players > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    /**
     * Tests whether new players can be added to the game as long as it's not full
     */
    @Test
    public void addPlayerDataTest() throws GameException, NicknameException, IllegalNumberOfPlayers, GameFullException {
        int minPlayers = GameParameters.getMinPlayers();
        int maxPlayers = GameParameters.getMaxPlayers();
        assertThrows(GameException.class, () -> {
            GameModel wrongGameModel = new GameModel(maxPlayers, new ServerSubject(), 0);
            wrongGameModel.addPlayerData("test", Content.RED);
            wrongGameModel.addPlayerData("test2", Content.RED);
        });
        assertThrows(NicknameException.class, () -> {
            GameModel wrongGameModel = new GameModel(maxPlayers, new ServerSubject(), 0);
            wrongGameModel.addPlayerData("test", Content.RED);
            wrongGameModel.addPlayerData("test", Content.PURPLE);
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
                serverSubject.subscribe("test" + i, testNetworkHandlers.get(i));
                gameModel.addPlayerData("test" + i, Content.values()[i]);
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
            assertThrows(GameFullException.class, () -> gameModel.addPlayerData("TEST", Content.RED));
        }
    }

    @Test
    void createPlayersTest() throws IllegalNumberOfPlayers, GameFullException, NicknameException, GameException {
        GameModel gameModel = new GameModel(2, new ServerSubject(), 0);
        gameModel.addPlayerData("test1", Content.RED);
        gameModel.addPlayerData("test2", Content.PURPLE);
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
            assertTrue(firstCardId >= GameParameters.getStartCardIndex(CardType.STARTER) &&
                    firstCardId <= GameParameters.getEndCardIndex(CardType.STARTER));
            assertEquals(GameParameters.getNumberOfResourceCardsInHand(), cards.stream().filter(c -> {
                int cardId = c.frontSide().getCardId();
                return cardId >= GameParameters.getStartCardIndex(CardType.RESOURCE) && cardId <=
                        GameParameters.getEndCardIndex(CardType.RESOURCE);
            }).toList().size());
            assertEquals(GameParameters.getNumberOfGoldCardsInHand(), cards.stream().filter(c -> {
                int cardId = c.frontSide().getCardId();
                return cardId >= GameParameters.getStartCardIndex(CardType.GOLD) && cardId <=
                        GameParameters.getEndCardIndex(CardType.GOLD);
            }).toList().size());
        }
    }

    /**
     * Tests if the getDrawableCards method returns the correct number of cards when queried.
     * Doesn't test if the cards are the same due to the decks' order being random.
     */
    @Test
    public void getDrawableCardsTest() throws GameException, GameFullException, NicknameException {
        GameModel gameModel;
        int playersNumber = 2;
        try {
            gameModel = new GameModel(playersNumber, new ServerSubject(), 0);
            gameModel.addPlayerData("resource", Content.GREEN);
            gameModel.addPlayerData("gold", Content.RED);
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
            assertTrue(playersNumber < GameParameters.getMinPlayers() ||
                    playersNumber > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    /**
     * Tests whether cards can be drawn from the Resource card deck and the Gold card deck
     */
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
            assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                    numberOfPlayers > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    /**
     * A testing method that assures the return of the winning players (who have the maximum number of points)
     */
    @Test
    public void getWinningPlayersTest() throws GameException, GameFullException, NicknameException {
        int numberOfPlayers = 2;
        try{
            GameModel gameModel = new GameModel(numberOfPlayers, new ServerSubject(), 0);
            ArrayList<String> nicknames = new ArrayList<>(Arrays.asList("test", "test2"));

            assertTrue(gameModel.getWinningPlayers().isEmpty());

            gameModel.addPlayerData(nicknames.getFirst(), Content.RED);
            gameModel.addPlayerData(nicknames.get(1), Content.BLUE);
            gameModel.createPlayers();

            Corner fakeCorner = new Corner(Content.WHITE, Location.TR);

            gameModel.getPlayer(nicknames.getFirst()).placeCard(CardBuilder.buildCard(9).frontSide(), fakeCorner);

            assertEquals(nicknames.getFirst(), gameModel.getWinningPlayers().getFirst());

            gameModel.getPlayer(nicknames.get(1)).placeCard(CardBuilder.buildCard(9).frontSide(), fakeCorner);

            assertEquals(nicknames.stream().sorted().toList(), gameModel.getWinningPlayers().stream().sorted().toList());

        }catch (IllegalNumberOfPlayers e){
            assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                    numberOfPlayers > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }

    @Test
    void isLobbyEmptyTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = GameParameters.getMinPlayers();
        int maxPlayers = GameParameters.getMaxPlayers();
        assertThrows(IllegalNumberOfPlayers.class, () -> new GameModel(minPlayers - 1, new ServerSubject(), 1));
        assertThrows(IllegalNumberOfPlayers.class, () -> new GameModel(maxPlayers + 1, new ServerSubject(), 1));
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertTrue(game.isLobbyEmpty());
        game.addPlayerData("test", Content.RED);
        assertFalse(game.isLobbyEmpty());
    }

    @Test
    void getNumberOfPlayersTest() throws IllegalNumberOfPlayers{
        int minPlayers = GameParameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertEquals(minPlayers, game.getNumberOfPlayers());
    }

    @Test
    void getLobbyNicknamesTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = GameParameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertEquals(new ArrayList<>(), game.getLobbyNicknames());
        game.addPlayerData("test", Content.RED);
        assertEquals(new ArrayList<>(List.of("test")), game.getLobbyNicknames());
        game.addPlayerData("test1", Content.BLUE);
        assertEquals(new ArrayList<>(Arrays.asList("test", "test1")), game.getLobbyNicknames());
    }

    @Test
    void checkNicknameTest() throws IllegalNumberOfPlayers, GameException, GameFullException, NicknameException {
        int minPlayers = GameParameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 1);
        assertTrue(game.checkNickname("test"));
        assertFalse(game.checkNickname("te st"));
        assertFalse(game.checkNickname(String.format("test%s", GameParameters.getDelimiter())));
        assertFalse(game.checkNickname(String.format("test%s", GameParameters.getCommandChar())));
        assertFalse(game.checkNickname("t".repeat(GameParameters.getMaxNicknameLength() + 1)));
        game.addPlayerData("test", Content.RED);
        assertFalse(game.checkNickname("test"));
    }

    @Test
    void deletePlayerDataTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException {
        String nickname = "test";
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nickname, handler1);
        serverSubject.subscribe("test2", handler2);

        int minPlayers = GameParameters.getMinPlayers();
        GameModel gameModel = new GameModel(minPlayers, serverSubject, 0);
        gameModel.addPlayerData(nickname, Content.RED);
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
        GameModel game = new GameModel(GameParameters.getMinPlayers(), new ServerSubject(), 0);
        int startCardIndex = GameParameters.getStartCardIndex(CardType.OBJECTIVE);
        int endCardIndex = GameParameters.getEndCardIndex(CardType.OBJECTIVE);
        int numberOfObjectives = endCardIndex - startCardIndex;
        for(int i = 0; i < numberOfObjectives / GameParameters.getNumberOfDrawnSecretObjectives(); i++){
            game.drawObjectiveCards();
        }
        assertThrows(DeckException.class, game::drawObjectiveCards);
    }
    
    @Test
    void isGameStuckTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException{
        int minPlayers = GameParameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 0);
        assertTrue(game.isGameStuck());
        game.addPlayerData("test", Content.RED);
        game.addPlayerData("test1", Content.BLUE);
        game.createPlayers();
        assertTrue(game.isGameStuck());
        Player player = game.getPlayer("test");
        player.placeStarterCard(CardBuilder.buildCard(GameParameters.getStartCardIndex(CardType.STARTER)).backSide());
        assertFalse(game.isGameStuck());
    }

    @Test
    void getCommonObjectivesTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException {
        int minPlayers = GameParameters.getMinPlayers();
        GameModel game = new GameModel(minPlayers, new ServerSubject(), 0);
        assertNotNull(game.getCommonObjectives());
        assertFalse(game.getCommonObjectives().isEmpty());
        game.addPlayerData("test", Content.GREEN);
        game.addPlayerData("test1", Content.PURPLE);
        game.createPlayers();
        assertEquals(game.getCommonObjectives(), game.getPlayer("test").getObjectives());
        assertEquals(game.getCommonObjectives(), game.getPlayer("test1").getObjectives());
    }

    @Test
    void exceptionDrawCardTest() throws IllegalNumberOfPlayers, NicknameException, GameFullException, GameException{
        GameModel game = new GameModel(2, new ServerSubject(), 1);
        game.addPlayerData("test", Content.RED);
        game.addPlayerData("test1", Content.BLUE);
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
            game.drawCard(game.getPlayer("test"), CardType.RESOURCE, GameParameters.getNumberOfVisibleCards() + 1);
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