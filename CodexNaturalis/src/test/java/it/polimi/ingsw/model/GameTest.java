package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.corner.Corner;
import it.polimi.ingsw.model.card.corner.Location;
import it.polimi.ingsw.model.deck.TurnDeck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guglielmo Gatti
 */
public class GameTest {

    private final int deckStart = GameParameters.getStartCardIndex(CardType.RESOURCE);
    private final int endBasicCards = GameParameters.getEndCardIndex(CardType.STARTER);

    @Test
    public void isLastTurnTest(){
        Game game;
        String playerName = "test";
        Content playerContent = Content.RED;
        for(int i = 0; i < 10; i++){
            try {
                game = new Game(i);
                game.addPlayer(playerName, playerContent);
                Player player = game.getPlayer(playerName);
                BasicCard starter = CardBuilder.buildCard(81).backSide();
                player.placeStarterCard(starter);
                assertFalse(game.isLastTurn());
                BasicCard previousCard = starter;
                for(int j = 0; j < 40; j++){
                    BasicCard card = CardBuilder.buildCard(10).frontSide();
                    card.setOwner(player);
                    Corner corner = previousCard.getAllCorners().stream()
                            .filter(c -> c.getLocation() == Location.TR)
                            .findFirst().orElseThrow();
                    player.placeCard(card,corner);
                    previousCard = card;
                }
                assertTrue(game.isLastTurn());
                game = new Game(i);
                game.addPlayer(playerName,playerContent);
                player = game.getPlayer(playerName);
                while(game.getDrawableCards().get(CardType.RESOURCE).getFirst() != null){
                    game.drawCard(player,CardType.RESOURCE,0);
                }
                while(game.getDrawableCards().get(CardType.RESOURCE).size() != 1){
                    game.drawCard(player,CardType.RESOURCE,1);
                }
                assertFalse(game.isLastTurn());
                while(game.getDrawableCards().get(CardType.GOLD).getFirst() != null){
                    game.drawCard(player,CardType.GOLD,0);
                }
                while(game.getDrawableCards().get(CardType.GOLD).size() != 1){
                    game.drawCard(player,CardType.GOLD,1);
                }
                assertTrue(game.isLastTurn());
            }
            catch (IllegalNumberOfPlayers e){
                assertTrue(i < GameParameters.getMinPlayers() ||
                        i > GameParameters.getMaxPlayers());
            }
        }
    }

    @Test
    public void addPlayerTest(){
        Game game;
        for(int numberOfPlayers = GameParameters.getMinPlayers(); numberOfPlayers <= GameParameters.getMaxPlayers(); numberOfPlayers++){
            try {
                game = new Game(numberOfPlayers);
                for(Content color : Arrays.stream(Content.values()).filter(Content::isColor).toList()){
                    try{
                        game.addPlayer(color.toString(), color);
                        assertNotNull(game.getPlayer(color.toString()));
                        assertEquals(color.toString(), game.getPlayer(color.toString()).getNickname());
                        assertEquals(color, game.getPlayer(color.toString()).getColor());
                    }
                    catch (GameException e){
                        if(game.isGameFull()){
                            assertEquals(game.getAllPlayers().size(), numberOfPlayers);
                            break;
                        }
                        assertTrue(game.getPlayer(color.toString()) != null ||
                            game.getAllPlayers().stream().anyMatch(p -> p.getColor().equals(color)));
                    }
                }
            }
            catch (IllegalNumberOfPlayers e){
                assertTrue(numberOfPlayers < GameParameters.getMinPlayers() ||
                        numberOfPlayers > GameParameters.getMaxPlayers());
            }
        }
    }
    
    @Test
    public void getDrawableCardsTest(){
        for(int numOfVisibleCards = 0; numOfVisibleCards < endBasicCards - deckStart; numOfVisibleCards++){
            TurnDeck<CardSides> deck = new TurnDeck<>(CardBuilder::buildCard, deckStart, endBasicCards, numOfVisibleCards);

        }
    }

    @Test
    public void drawCardTest() throws IllegalNumberOfPlayers{
        Game game = new Game(2);
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
}