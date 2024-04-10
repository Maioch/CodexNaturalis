package it.polimi.ingsw.model.deck;

import static org.junit.jupiter.api.Assertions.*;
import it.polimi.ingsw.model.card.CardSides;
import org.junit.jupiter.api.Test;

import it.polimi.ingsw.model.card.BasicCard;
import java.util.ArrayList;

/**
 * @author Andrea Fidanza
 */
public class TurnDeckTest {
    private final int range_start = 1;
    private final int range_end = 20;
    private final int numberOfVisibleCards = 2;

    /**
     * Tests the drawVisibleCard method by drawing visible cards until the deck is empty
     */
    @Test
    void drawVisibleCardTest(){
        TurnDeck deck = new TurnDeck(range_start, range_end, numberOfVisibleCards);
        int index = 0;
        try {
            while(!deck.isEmpty()) {
                for (index = 0; index < numberOfVisibleCards; index++) {
                    ArrayList<CardSides> cards = new ArrayList<>(deck.getCards());
                    ArrayList<CardSides> visibleCards = new ArrayList<>(deck.getVisibleCards());
                    CardSides card = deck.drawVisibleCard(index);
                    assertTrue(visibleCards.contains(card));
                    if (!deck.isEmpty())
                        assertTrue(cards.contains(deck.getVisibleCards().get(index)));
                    visibleCards = new ArrayList<>(deck.getVisibleCards());
                    assertFalse(visibleCards.contains(card));
                }
            }
        } catch (RuntimeException ex){
            assertNull(deck.getVisibleCards().get(index));
        }
    }

    /**
     * Tests the getCardOnTop method
     */
    @Test
    void getCardOnTopTest(){
        TurnDeck deck = new TurnDeck(range_start, range_end, numberOfVisibleCards);
        BasicCard topDeck = deck.getCardOnTop();
        assertEquals(topDeck, deck.draw().backSide());
    }

    /**
     * Tests the equals method
     */
    @Test
    void equalsTest(){
        TurnDeck deck = new TurnDeck(range_start, range_end, numberOfVisibleCards);
        TurnDeck otherDeck = new TurnDeck(range_start, range_end, numberOfVisibleCards);
        assertEquals(deck, otherDeck);
        otherDeck.drawVisibleCard(0);
        assertNotEquals(deck, otherDeck);
    }
}