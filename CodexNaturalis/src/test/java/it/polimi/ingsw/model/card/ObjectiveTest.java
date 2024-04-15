package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Location;
import it.polimi.ingsw.model.Player;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Objective and its Bonus implementations
 * @author Guglielmo Gatti, Francesco Saverio Nisoli
 */

public class ObjectiveTest {
    private final int startObjective = 87;
    private final int endObjective = 102;

    @Test
    void checkObjectiveTest(){
        Objective referenceContentObjective = CardBuilder.buildObjective(95);
        Objective referencePatternObjective = CardBuilder.buildObjective(87);

        Player referencePlayer = new Player(
                "test",
                Content.RED,
                new ArrayList<>(Arrays.asList(
                        CardBuilder.buildCard(72),
                        CardBuilder.buildCard(73),
                        CardBuilder.buildCard(74)
                )),
                new ArrayList<>(Arrays.asList(
                        referenceContentObjective,
                        referencePatternObjective
                )
                )
        );

        BasicCard starter = CardBuilder.buildCard(83).frontSide();
        starter.setOwner(referencePlayer);
        referencePlayer.placeStarterCard(starter);

        BasicCard card1 = CardBuilder.buildCard(2).frontSide();
        card1.setOwner(referencePlayer);
        referencePlayer.placeCard(card1, starter.getAllCorners().stream().
                filter(c -> c.getLocation() == Location.TL)
                .findFirst().orElseThrow());

        assertEquals(2, referenceContentObjective.checkObjective());
        assertEquals(0, referencePatternObjective.checkObjective());

        BasicCard card2 = CardBuilder.buildCard(4).frontSide();
        card2.setOwner(referencePlayer);
        referencePlayer.placeCard(card2, card1.getAllCorners().stream().
                filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow());

        assertEquals(2, referenceContentObjective.checkObjective());
        assertEquals(0, referencePatternObjective.checkObjective());

        BasicCard card3 = CardBuilder.buildCard(47).frontSide();
        card3.setOwner(referencePlayer);
        referencePlayer.placeCard(card3, card2.getAllCorners().stream().
                filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow());

        assertEquals(2, referenceContentObjective.checkObjective());
        assertEquals(2, referencePatternObjective.checkObjective());
    }

    @Test
    void equalsTest(){
        Objective otherObjective = null;
        for(int id = startObjective; id <= endObjective; id++){
            Objective objective = CardBuilder.buildObjective(id);
            if(otherObjective != null){
                assertNotEquals(objective, otherObjective);
            }
            Objective sameObjective = CardBuilder.buildObjective(id);
            assertEquals(objective, sameObjective);
            otherObjective = objective;
        }
    }
}