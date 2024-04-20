package it.polimi.ingsw.model.deck;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.model.GameParameters;
import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.Objective;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

/**
 * @author Guglielmo Gatti
 */
public class DeckTest {
    private final int deckStart = GameParameters.getStartCardIndex(GameParameters.CardType.RESOURCE);
    private final int endBasicCards = GameParameters.getEndCardIndex(GameParameters.CardType.STARTER);
    private final int startObjectives = GameParameters.getStartCardIndex(GameParameters.CardType.OBJECTIVE);
    private final int deckEnd = GameParameters.getEndCardIndex(GameParameters.CardType.OBJECTIVE);
    private final ArrayList<Integer> ids = new ArrayList<>();

    /**
     * Tests taking a deck and drawing all the cards from it.
     * used to test the draw method and the isEmpty method
     */
    @Test
    public void repeatedDrawTest() {
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

    private void checkId(int idToCheck, int deckStart, int deckEnd) {
        for (int id : ids) {
            assertNotEquals(idToCheck, id);
        }
        ids.add(idToCheck);
        assertTrue(idToCheck <= deckEnd && idToCheck >= deckStart);
    }
}