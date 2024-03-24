package it.polimi.ingsw.model;

import it.polimi.ingsw.model.deck.Deck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Guglielmo Gatti
 */

public class DeckTest {
    private final int deckStart = 25;
    private final int deckEnd = 42;

    private final ArrayList<Integer> ids = new ArrayList<Integer>();
    private final Deck deck = new Deck(deckStart,deckEnd);

    /**
     * Tests taking a deck and drawing all the cards from it.
     * used to test the draw method and the isEmpty method
     */
    @Test
    public void RepeatedDrawTest(){
        for(int i = deckStart; i <= deckEnd; i++){
            try{
                int newId = deck.draw();
                for (Integer id : ids) {
                    assertFalse(newId == id);
                }
                ids.add(newId);
                assertTrue(deck.draw() <= deckEnd && deck.draw() >= deckStart);
                assertFalse(deck.isEmpty());
            }
            catch(Deck.NoMoreCardsException noCards){
                assertTrue(deck.isEmpty());
            }
        }
    }
}
