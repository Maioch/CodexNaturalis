package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.corner.Location;
import it.polimi.ingsw.model.deck.TurnDeck;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.LinkedHashMap;

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
        LinkedHashMap<Integer, Location> placements = new LinkedHashMap<>(){{
            for(int i = 0; i < 46; i++){
                put(i,Location.TR);
            }
        }};
        for(int i = 0; i < 10; i++){
            try {
                game = new Game(i);
                game.addPlayer(playerName, playerContent);
                Player player = game.getPlayer(playerName);
                BasicCard starter = CardBuilder.buildCard(81).backSide();
                player.placeStarterCard(starter);
                assertFalse(game.isLastTurn());
                TestUtilities.createTestBoard(player,placements,starter,true);
                assertTrue(game.isLastTurn());
                game = new Game(i);
                game.addPlayer(playerName,playerContent);
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
        for(int numberOfPlayers = GameParameters.getMinPlayers() - 1; numberOfPlayers < GameParameters.getMaxPlayers() + 1; numberOfPlayers++){
            try {
                game = new Game(numberOfPlayers);
                for(Content color : Arrays.stream(Content.values()).filter(Content::isColor).toList()){
                    try{
                        game.addPlayer(color.toString(), color);
                        assertNotNull(game.getPlayer(color.toString()));
                        assertEquals(color.toString(), game.getPlayer(color.toString()).getNickname());
                        assertEquals(color, game.getPlayer(color.toString()).getColor());
                    }
                    catch (UsernameTakenException | ColorTakenException e){
                        assertTrue(game.getPlayer(color.toString()) != null ||
                            game.getAllPlayers().stream().anyMatch(p -> p.getColor().equals(color)));
                    }
                    catch (GameFullException e){
                        assertEquals(game.getAllPlayers().size(), numberOfPlayers);
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
        for(int numOfVisibleCards = 0; numOfVisibleCards < 999999999; numOfVisibleCards++){
            TurnDeck<CardSides> resourceDeck = new TurnDeck<>(CardBuilder::buildCard, deckStart, endBasicCards, numOfVisibleCards);
            TurnDeck<CardSides> goldDeck = new TurnDeck<>(CardBuilder::buildCard, deckStart, endBasicCards, numOfVisibleCards);
        }
    }
}