package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.model.server.TestUtilities;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.List;
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
        Objective referenceContentObjective = CardBuilder.buildObjective(96);
        Objective referencePatternObjective = CardBuilder.buildObjective(87);
        Objective referencePatternObjective2 = CardBuilder.buildObjective(94);

        Player referencePlayer = new Player("test", Content.RED, new ArrayList<>(),
                new ArrayList<>(Arrays.asList(
                        referenceContentObjective,
                        referencePatternObjective
                )), new ServerSubject());

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

        referencePlayer = new Player("test",
                Content.RED,
                new ArrayList<>(),
                new ArrayList<>(List.of(
                        referencePatternObjective2
                )),
                new ServerSubject());

        starter = CardBuilder.buildCard(81).frontSide();
        starter.setOwner(referencePlayer);
        referencePlayer.placeStarterCard(starter);

        LinkedHashMap<Integer,Location> placements2 = new LinkedHashMap<>(){{
            put(21,Location.TR);
            put(31,Location.BR);
            put(2, Location.BR);
            put(32, Location.BL);
            put(22, Location.BR);
            put(33, Location.BR);
            put(3, Location.BR);
            put(34, Location.BL);
        }};
        TestUtilities.createTestBoard(referencePlayer, placements2, starter,true);

        assertEquals(6, referencePatternObjective2.checkObjective());
        Objective referencePatternObjective3 = CardBuilder.buildObjective(88);

        referencePlayer = new Player("test",
                Content.RED,
                new ArrayList<>(),
                new ArrayList<>(List.of(
                        referencePatternObjective3
                )),
                new ServerSubject());

        starter = CardBuilder.buildCard(81).frontSide();
        starter.setOwner(referencePlayer);
        referencePlayer.placeStarterCard(starter);

        LinkedHashMap<Integer,Location> placements3 = new LinkedHashMap<>(){{
            put(11,Location.TL);
            put(12,Location.TL);
            put(13, Location.TL);
            put(14, Location.TL);
            put(15, Location.TL);
            put(16, Location.TL);
        }};
        TestUtilities.createTestBoard(referencePlayer, placements3, starter,true);

        assertEquals(4, referencePatternObjective3.checkObjective());
    }

    @Test
    void getPointsTest(){
        Objective objective = CardBuilder.buildObjective(87);
        assertEquals(2, objective.getPoints());
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
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(otherObjective, "Test");
    }

    @Test
    void wrongObjectiveTest(){
        Objective objective = new Objective(1, 1);
        assertThrows(RuntimeException.class, () -> objective.new PatternBonus(new HashMap<>(){{
            put(new Point(0, 0), Content.PEN);
        }}));
        assertThrows(RuntimeException.class, () -> objective.new PatternBonus(new HashMap<>(){{
            put(new Point(0, 0), Content.EMPTY);
        }}));
        assertThrows(RuntimeException.class, () -> objective.new PatternBonus(new HashMap<>(){{
            put(new Point(0, 0), Content.WHITE);
        }}));
        assertThrows(RuntimeException.class, () -> objective.new ContentBonus(new ArrayList<>(){{
            add(Content.WHITE);
        }}));
        assertThrows(RuntimeException.class, () -> objective.new ContentBonus(new ArrayList<>(){{
            add(Content.EMPTY);
        }}));
    }
}