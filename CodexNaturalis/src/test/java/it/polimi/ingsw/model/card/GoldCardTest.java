package it.polimi.ingsw.model.card;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.Content;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;

public class GoldCardTest {
    private final int startGold = 41;
    private final int endGold = 80;

    @Test
    void getRequirementsTest(){
        for(int id = startGold; id <= endGold; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            assertEquals(card.getClass(), GoldCard.class);
            GoldCard gold = (GoldCard) card;
            ArrayList<Content> actualRequirements = CardBuilder.getContentFromArray(node, "requirements");
            HashMap<Content,Integer> actualRequirementsMap = new HashMap<>(){{
                for(Content content : Content.values()){
                    put(content, actualRequirements.stream()
                            .filter(x -> x == content)
                            .mapToInt(x -> 1)
                            .reduce(0,Integer::sum));
                }
            }};
            assertEquals(
                    actualRequirementsMap,
                    gold.getRequirements());
        }
    }

    @Test
    void getPointsTest(){

    }

    @Test
    void equalsTest(){
        GoldCard otherGold = null;
        for(int id = startGold; id <= endGold; id++) {
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            assertEquals(card.getClass(), GoldCard.class);
            GoldCard gold = (GoldCard) card;
            if(otherGold != null){
                assertNotEquals(gold, otherGold);
            }
            GoldCard sameGold = (GoldCard) CardBuilder.buildCard(id).frontSide();
            assertEquals(gold, sameGold);
            otherGold = gold;
        }
    }
}