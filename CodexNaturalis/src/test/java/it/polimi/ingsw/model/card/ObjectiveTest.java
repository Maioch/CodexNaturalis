package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.TestUtilities;
import it.polimi.ingsw.model.card.corner.Location;
import it.polimi.ingsw.model.Player;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Objective and its Bonus implementations
 * @author Guglielmo Gatti, Francesco Saverio Nisoli
 */

public class ObjectiveTest {

    /**
     * Testing method that assures a correct computation of the points given by the objective
     */
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
                ))
        );
        LinkedHashMap<Integer,Location> placements1 = new LinkedHashMap<>(){{
            put(2,Location.TR);
            put(3,Location.TR);
            put(4, Location.TR);
            put(14, Location.BR);
            put(15, Location.TR);
            put(16, Location.TR);
            put(5, Location.TL);
            put(6, Location.BL);
            put(17, Location.TL);
            put(18, Location.TR);
            put(19, Location.TR);
            put(7, Location.BR);
        }};

        BasicCard starter = CardBuilder.buildCard(83).frontSide();
        starter.setOwner(referencePlayer);
        referencePlayer.placeStarterCard(starter);

        TestUtilities.createTestBoard(referencePlayer, placements1, starter,true);

        assertEquals(4, referenceContentObjective.checkObjective());
        assertEquals(4, referencePatternObjective.checkObjective());
    }


    @Test
    void equalsTest(){
        Objective otherObjective = null;
        int endObjective = 102;
        int startObjective = 87;
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