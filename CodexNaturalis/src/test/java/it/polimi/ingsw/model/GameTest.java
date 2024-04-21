package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.corner.Corner;
import it.polimi.ingsw.model.card.corner.Location;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guglielmo Gatti
 */
public class GameTest {

    private final int resourceSize = GameParameters.getEndCardIndex(CardType.RESOURCE) - GameParameters.getStartCardIndex(CardType.RESOURCE) + 1;
    private final int goldSize = GameParameters.getEndCardIndex(CardType.GOLD) - GameParameters.getStartCardIndex(CardType.GOLD) + 1;
    private final String playerNumberFail = "please set a different number of players for the Game instance" +
            " or the tests won't be able to run correctly";

    /**
     * Tests whether the game class can correctly assess whether a game should end during the next turn or not
     */
    @Test
    public void isLastTurnTest() {
        Game game;
        String playerName = "test";
        Content playerContent = Content.RED;
        int players = 2;
        try {
            game = new Game(players);
            game.addPlayer(playerName, playerContent);
            Player player = game.getPlayer(playerName);
            BasicCard starter = CardBuilder.buildCard(81).backSide();
            player.placeStarterCard(starter);
            assertFalse(game.isLastTurn());
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
            assertTrue(game.isLastTurn());
            game = new Game(players);
            game.addPlayer(playerName, playerContent);
            player = game.getPlayer(playerName);
            while (game.getDrawableCards().get(CardType.RESOURCE).getFirst() != null) {
                game.drawCard(player, CardType.RESOURCE, 0);
            }
            while (game.getDrawableCards().get(CardType.RESOURCE).size() != 1) {
                game.drawCard(player, CardType.RESOURCE, 1);
            }
            assertFalse(game.isLastTurn());
            while (game.getDrawableCards().get(CardType.GOLD).getFirst() != null) {
                game.drawCard(player, CardType.GOLD, 0);
            }
            while (game.getDrawableCards().get(CardType.GOLD).size() != 1) {
                game.drawCard(player, CardType.GOLD, 1);
            }
            assertTrue(game.isLastTurn());
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
    public void addPlayerTest() {
        Game game;
        for (int numberOfPlayers = GameParameters.getMinPlayers(); numberOfPlayers <= GameParameters.getMaxPlayers(); numberOfPlayers++) {
            try {
                game = new Game(numberOfPlayers);
                for (Content color : Arrays.stream(Content.values()).filter(Content::isColor).toList()) {
                    try {
                        game.addPlayer(color.toString(), color);
                        assertNotNull(game.getPlayer(color.toString()));
                        assertEquals(color.toString(), game.getPlayer(color.toString()).getNickname());
                        assertEquals(color, game.getPlayer(color.toString()).getColor());
                    } catch (GameException e) {
                        if (game.isGameFull()) {
                            assertEquals(game.getAllPlayers().size(), numberOfPlayers);
                            break;
                        }
                        assertTrue(game.getPlayer(color.toString()) != null ||
                                game.getAllPlayers().stream().anyMatch(p -> p.getColor().equals(color)));
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
    public void getDrawableCardsTest() {
        Game game;
        int playersNumber = 2;
        try {
            game = new Game(playersNumber);
            game.addPlayer("resource", Content.GREEN);
            game.addPlayer("gold", Content.RED);
            Player resource = game.getPlayer("resource");
            Player gold = game.getPlayer("gold");
            int currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);

            for(int i = 0; i < resourceSize - 7; i++){
                game.drawCard(resource, CardType.RESOURCE, 0);
                currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
                assertEquals(6, currentlyDrawable);
            }
            for(int i = 0; i < goldSize - 5; i++){
                game.drawCard(gold, CardType.GOLD, 0);
                currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
                assertEquals(6, currentlyDrawable);
            }

            game.drawCard(resource, CardType.RESOURCE, 0);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);
            assertNull(game.getDrawableCards().get(CardType.RESOURCE).getFirst());
            game.drawCard(gold, CardType.GOLD, 0);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(6, currentlyDrawable);
            assertNull(game.getDrawableCards().get(CardType.GOLD).getFirst());
            game.drawCard(resource, CardType.RESOURCE, 1);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(5, currentlyDrawable);
            game.drawCard(gold, CardType.GOLD, 1);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(4, currentlyDrawable);
            game.drawCard(resource, CardType.RESOURCE, 1);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(3, currentlyDrawable);
            game.drawCard(gold, CardType.GOLD, 1);
            currentlyDrawable = game.getDrawableCards().get(CardType.RESOURCE).size() + game.getDrawableCards().get(CardType.GOLD).size();
            assertEquals(2, currentlyDrawable);
            assertTrue(game.isLastTurn());
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
    public void drawCardTest(){
        int numberOfPlayers = 2;
        try {
            Game game = new Game(numberOfPlayers);
            Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>());
            int index = 0;
            game.drawCard(playerTest, CardType.RESOURCE, index);
            assertFalse(playerTest.getHandCards().isEmpty());
            game.drawCard(playerTest, CardType.GOLD, index);
            assertEquals(playerTest.getHandCards().size(), 2);
            index = 1;
            game.drawCard(playerTest, CardType.RESOURCE, index);
            assertEquals(playerTest.getHandCards().size(), 3);
            game.drawCard(playerTest, CardType.GOLD, index);
            assertEquals(playerTest.getHandCards().size(), 4);
        }
        catch (IllegalNumberOfPlayers e){
            assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                    numberOfPlayers > GameParameters.getMaxPlayers());
            fail(playerNumberFail);
        }
    }
}