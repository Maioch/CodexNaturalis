package it.polimi.ingsw.model;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.card.CardBuilder;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

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
            JsonNode node = CardBuilder.getCardJson(id, "objectiveCards");
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