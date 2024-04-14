package it.polimi.ingsw.model.card;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** not currently implemented, waiting for Bonus implementations to be available
 * @author Guglielmo Gatti
 */

public class ObjectiveTest {
    private final int startObjective = 87;
    private final int endObjective = 102;

    @Test
    void checkObjectiveTest(){

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