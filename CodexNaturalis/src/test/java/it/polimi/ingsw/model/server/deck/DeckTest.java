package it.polimi.ingsw.model.server.deck;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.exceptions.DeckException;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author Guglielmo Gatti
 */
public class DeckTest {
    private final int deckStart = GameParameters.getStartCardIndex(CardType.RESOURCE);
    private final int endBasicCards = GameParameters.getEndCardIndex(CardType.STARTER);
    private final int startObjectives = GameParameters.getStartCardIndex(CardType.OBJECTIVE);
    private final int deckEnd = GameParameters.getEndCardIndex(CardType.OBJECTIVE);
    private final ArrayList<Integer> ids = new ArrayList<>();

    /**
     * Tests taking a deck and drawing all the cards from it.
     * used to test the draw method and the isEmpty method
     */
    @Test
    public void repeatedDrawTest() {
        assertThrows(DeckException.class, () -> new Deck<>(CardBuilder::buildCard, endBasicCards, deckStart));
        assertThrows(DeckException.class, () -> new Deck<>(CardBuilder::buildCard, 0, endBasicCards));
        Deck<CardSides> deckBasicCards = new Deck<>(CardBuilder::buildCard, deckStart, endBasicCards);
        Deck<Objective> deckObjectives = new Deck<>(CardBuilder::buildObjective, startObjectives, deckEnd);
        for (int i = deckStart; i <= endBasicCards + 1; i++) {
            try {
                CardSides cardSides = deckBasicCards.draw();
                int newId = cardSides.frontSide().getCardId();
                checkId(newId, deckStart, endBasicCards);
                assertEquals(deckBasicCards.isEmpty(), i == endBasicCards);
            } catch (RuntimeException noCards) {
                assertTrue(deckBasicCards.isEmpty());
            }
        }
        for(int i = startObjectives; i <= deckEnd + 1; i++){
            try {
                Objective objective = deckObjectives.draw();
                int newId = objective.getObjectiveId();
                checkId(newId, startObjectives, deckEnd);
                assertEquals(deckObjectives.isEmpty(), i == deckEnd);
            } catch (RuntimeException noCards) {
                assertTrue(deckObjectives.isEmpty());
            }
        }
    }

    /**
     * Helper method for repeatedDrawTest
     */
    private void checkId(int idToCheck, int deckStart, int deckEnd) {
        for (int id : ids) {
            assertNotEquals(idToCheck, id);
        }
        ids.add(idToCheck);
        assertTrue(idToCheck <= deckEnd && idToCheck >= deckStart);
    }
}