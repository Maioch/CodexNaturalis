package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
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

        createTestBoard(referencePlayer, placements1, starter,true);

        assertEquals(4, referenceContentObjective.checkObjective());
        assertEquals(4, referencePatternObjective.checkObjective());
    }

    /**
     * Helper method used to automatically place a set of cards on the player's board
     * @param player the player who owns the board on which the method will place the cards on
     * @param relativePlacements a LinkedHashMap (to preserve insertion order) which pairs the id of each card
     *                           to the position of the corner it's going to be placed on
     * @param base a card that has already been placed to use as a starting point
     * @param useBack whether to retrieve the back sides or the front sides when placing the cards
     * @return the last card placed
     */
    private BasicCard createTestBoard(Player player, LinkedHashMap<Integer,Location> relativePlacements, BasicCard base, boolean useBack){
        BasicCard previousCard = base;
        for(Map.Entry<Integer, Location> entry : relativePlacements.entrySet()){
            CardSides sides = CardBuilder.buildCard(entry.getKey());
            BasicCard card = useBack ? sides.backSide() : sides.frontSide();
            card.setOwner(player);
            player.placeCard(card, previousCard.getAllCorners().stream()
                    .filter(c -> c.getLocation() == entry.getValue())
                    .findFirst().orElseThrow());
            previousCard = card;
        }
        return previousCard;
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