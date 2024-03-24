package it.polimi.ingsw.model.deck;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author Andrea Fidanza
 */
public class TurnDeckTest {
    private final int range_start = 0;
    private final int range_end = 20;
    private final int numberOfVisibleCards = 2;
    private final TurnDeck deck = new TurnDeck(range_start, range_end, numberOfVisibleCards);

    @Test
    void drawVisibleCardTest(){
        int index = 0;
        try {
            while(!deck.isEmpty()) {
                for (index = 0; index < numberOfVisibleCards; index++) {
                    ArrayList<Integer> cards = new ArrayList<>(deck.getCards());
                    ArrayList<Integer> visibleCards = new ArrayList<>(deck.getVisibleCards());

                    int card = deck.drawVisibleCard(index);

                    assertTrue(visibleCards.contains(card));
                    if (!deck.isEmpty())
                        assertTrue(cards.contains(deck.getVisibleCards().get(index)));

                    visibleCards = new ArrayList<>(deck.getVisibleCards());
                    assertFalse(visibleCards.contains(card));
                }
            }
        } catch (Deck.NoMoreCardsException ex){
            assertNull(deck.getVisibleCards().get(index));
        }
    }
}