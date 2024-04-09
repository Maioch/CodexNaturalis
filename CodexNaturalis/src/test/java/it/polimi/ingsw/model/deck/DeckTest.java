package it.polimi.ingsw.model.deck;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.model.card.CardSides;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author Guglielmo Gatti
 */
public class DeckTest {
    private final int deckStart = 25;
    private final int deckEnd = 67;

    private final ArrayList<Integer> ids = new ArrayList<>();
    private final Deck deck = new Deck(deckStart,deckEnd);

    /**
     * Tests taking a deck and drawing all the cards from it.
     * used to test the draw method and the isEmpty method
     */
    @Test
    public void RepeatedDrawTest(){
        for(int i = deckStart; i <= deckEnd + 1; i++){
            try{
                CardSides cardSides = deck.draw();
                int newId = cardSides.frontSide().getCardId();
                for (int id : ids) {
                    assertNotEquals(newId,  id);
                }
                ids.add(newId);
                assertTrue(newId <= deckEnd && newId >= deckStart);
                assertEquals(deck.isEmpty(),i == deckEnd);
            }
            catch(RuntimeException noCards){
                assertTrue(deck.isEmpty());
            }
        }
    }

    /**
     * Tests the equals method
     */
    @Test
    public void equalsTest(){
        Deck otherDeck = new Deck(deckStart, deckEnd);
        assertEquals(deck, otherDeck);
        otherDeck.draw();
        assertNotEquals(deck, otherDeck);
    }
}
