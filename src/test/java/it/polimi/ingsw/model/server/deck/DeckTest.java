package it.polimi.ingsw.model.server.deck;

import it.polimi.ingsw.exceptions.DeckException;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class DeckTest {

    private final int deckStart = Parameters.getStartCardIndex(CardType.RESOURCE);
    private final int endBasicCards = Parameters.getEndCardIndex(CardType.STARTER);
    private final int startObjectives = Parameters.getStartCardIndex(CardType.OBJECTIVE);
    private final int deckEnd = Parameters.getEndCardIndex(CardType.OBJECTIVE);
    private final ArrayList<Integer> ids = new ArrayList<>();

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

    private void checkId(int idToCheck, int deckStart, int deckEnd) {
        for (int id : ids) {
            assertNotEquals(idToCheck, id);
        }
        ids.add(idToCheck);
        assertTrue(idToCheck <= deckEnd && idToCheck >= deckStart);
    }
}