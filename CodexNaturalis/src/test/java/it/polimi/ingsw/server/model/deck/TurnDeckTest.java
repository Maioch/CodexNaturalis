package it.polimi.ingsw.server.model.deck;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.server.model.card.CardBuilder;
import it.polimi.ingsw.server.model.card.CardSides;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author Andrea Fidanza
 */
public class TurnDeckTest {
    private final int deckStart = 1;
    private final int deckEnd = 20;
    private final int numberOfVisibleCards = 2;

    /**
     * Tests the drawVisibleCard method by drawing visible cards until the deck is empty
     */
    @Test
    void drawVisibleCardTest(){
        TurnDeck<CardSides> deck = new TurnDeck<>(CardBuilder::buildCard, deckStart, deckEnd, numberOfVisibleCards);
        int index = 0;
        try {
            while(!deck.isEmpty()) {
                for (index = 0; index < numberOfVisibleCards; index++) {
                    ArrayList<CardSides> cards = new ArrayList<>(deck.getDeck());
                    ArrayList<CardSides> visibleCards = new ArrayList<>(deck.getVisibleElements());
                    CardSides card = deck.drawVisibleElement(index);
                    assertTrue(visibleCards.contains(card));
                    if (!deck.isEmpty())
                        assertTrue(cards.contains(deck.getVisibleElements().get(index)));
                    visibleCards = new ArrayList<>(deck.getVisibleElements());
                    assertFalse(visibleCards.contains(card));
                }
            }
        } catch (RuntimeException ex){
            assertNull(deck.getVisibleElements().get(index));
        }
    }

    /**
     * Tests the getCardOnTop method
     */
    @Test
    void getCardOnTopTest(){
        TurnDeck<CardSides> deck = new TurnDeck<>(CardBuilder::buildCard, deckStart, deckEnd, numberOfVisibleCards);
        CardSides topDeck = deck.getElementOnTop();
        assertEquals(topDeck, deck.draw());
    }
}