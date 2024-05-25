package it.polimi.ingsw.model.server;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameModel;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.server.ServerSubject;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

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
    public void isLastTurnTest() throws GameException, GameFullException, NicknameTakenException{
        GameModel gameModel;
        String playerName = "test";
        Content playerContent = Content.RED;
        int players = 2;
        try {
            gameModel = new GameModel(players, new ServerSubject());
            gameModel.addPlayer(playerName, playerContent);
            Player player = gameModel.getPlayer(playerName);
            BasicCard starter = CardBuilder.buildCard(81).backSide();
            player.placeStarterCard(starter);
            assertFalse(gameModel.isLastTurn());
            BasicCard previousCard = starter;
            for (int j = 0; j < 40; j++) {
                BasicCard card = CardBuilder.buildCard(10).frontSide();
                card.setOwner(player);
                Corner corner = previousCard.getAllCorners().stream()
                        .filter(c -> c.getLocation() == Location.TR)
                        .findFirst().orElseThrow();
                player.placeCard(card, corner);
                previousCard = card;
            }
            assertTrue(gameModel.isLastTurn());
            gameModel = new GameModel(players, new ServerSubject());
            gameModel.addPlayer(playerName, playerContent);
            player = gameModel.getPlayer(playerName);
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).getFirst() != null) {
                gameModel.drawCard(player, CardType.RESOURCE, 0);
            }
            while (gameModel.getDrawableCards().get(CardType.RESOURCE).size() != 1) {
                gameModel.drawCard(player, CardType.RESOURCE, 1);
            }
            assertFalse(gameModel.isLastTurn());
            while (gameModel.getDrawableCards().get(CardType.GOLD).getFirst() != null) {
                gameModel.drawCard(player, CardType.GOLD, 0);
            }
            while (gameModel.getDrawableCards().get(CardType.GOLD).size() != 1) {
                gameModel.drawCard(player, CardType.GOLD, 1);
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
    public void addPlayerTest() throws GameException, NicknameTakenException{
        GameModel gameModel;
        for (int numberOfPlayers = GameParameters.getMinPlayers(); numberOfPlayers <= GameParameters.getMaxPlayers(); numberOfPlayers++) {
            try {
                gameModel = new GameModel(numberOfPlayers, new ServerSubject());
                for (Content color : Arrays.stream(Content.values()).filter(Content::isColor).toList()) {
                    try {
                        gameModel.addPlayer(color.toString(), color);
                        assertNotNull(gameModel.getPlayer(color.toString()));
                        assertEquals(color.toString(), gameModel.getPlayer(color.toString()).getNickname());
                        assertEquals(color, gameModel.getPlayer(color.toString()).getColor());
                    } catch (GameFullException e) {
                        if (gameModel.isGameFull()) {
                            assertEquals(gameModel.getAllPlayers().size(), numberOfPlayers);
                            break;
                        }
                        assertTrue(gameModel.getPlayer(color.toString()) != null ||
                                gameModel.getAllPlayers().stream().anyMatch(p -> p.getColor().equals(color)));
                    }
                }
            } catch (IllegalNumberOfPlayers e) {
                assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                        numberOfPlayers > GameParameters.getMaxPlayers());
            }
        }
    }

    /**
     * Tests if the getDrawableCards method returns the correct number of cards when queried.
     * Doesn't test if the cards are the same due to the decks' order being random.
     */
    @Test
    public void getDrawableCardsTest() throws GameException, GameFullException, NicknameTakenException{
        GameModel gameModel;
        int playersNumber = 2;
        try {
            gameModel = new GameModel(playersNumber, new ServerSubject());
            gameModel.addPlayer("resource", Content.GREEN);
            gameModel.addPlayer("gold", Content.RED);
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
        int numberOfPlayers = 2;
        try {
            GameModel gameModel = new GameModel(numberOfPlayers, new ServerSubject());
            Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
            int index = 0;
            gameModel.drawCard(playerTest, CardType.RESOURCE, index);
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
    public void getWinningPlayersTest() throws GameException, GameFullException, NicknameTakenException{
        int numberOfPlayers = 2;
        try{
            GameModel gameModel = new GameModel(numberOfPlayers, new ServerSubject());
            Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
            Player playerTest2 = new Player("test2", Content.BLUE, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
            ArrayList<String> nicknames = new ArrayList<>(Arrays.asList(playerTest.getNickname(), playerTest2.getNickname()));

            assertTrue(gameModel.getWinningPlayers().isEmpty());

            gameModel.addPlayer(nicknames.getFirst(), playerTest.getColor());
            gameModel.addPlayer(nicknames.get(1), playerTest2.getColor());

            Corner fakeCorner = new Corner(Content.WHITE, Location.TR);

            for(int i = GameParameters.getStartCardIndex(CardType.RESOURCE); i <= GameParameters.getEndCardIndex(CardType.GOLD); i++){
                playerTest.placeCard(CardBuilder.buildCard(i).backSide(), fakeCorner);
            }

            assertEquals(nicknames.getFirst(), gameModel.getWinningPlayers().getFirst());

            for(int i = GameParameters.getStartCardIndex(CardType.RESOURCE); i <= GameParameters.getEndCardIndex(CardType.GOLD); i++){
                playerTest2.placeCard(CardBuilder.buildCard(i).backSide(), fakeCorner);
            }

            assertEquals(nicknames, gameModel.getWinningPlayers());

        }catch (IllegalNumberOfPlayers e){
            assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                    numberOfPlayers > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }
}