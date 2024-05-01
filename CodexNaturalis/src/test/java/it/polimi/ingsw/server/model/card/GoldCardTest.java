package it.polimi.ingsw.server.model.card;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.GoldCard;
import it.polimi.ingsw.network.server.ServerSubject;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.model.server.Player;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
        int startResource = 1;
        int endResource = 40;
        Player playerTest = new Player("test", Content.RED, new ArrayList<>(){{
            for(int i = startResource; i <= endResource; i++)
                add(CardBuilder.buildCard(i));
        }}, new ArrayList<>(), new ServerSubject());
        int offset = 0;
        for(CardSides card : playerTest.getHandCards()){
            Corner cornerTest = new Corner(Content.WHITE, Location.BL);
            cornerTest.setX(offset);
            cornerTest.setY(offset);
            playerTest.placeCard(card.frontSide(), cornerTest);
            offset += 2;
        }
        ArrayList<BasicCard> placedCard = playerTest.getPlacedCards();
        Iterator<BasicCard> iterator = placedCard.iterator();
        BasicCard currentCard = iterator.next();
        for(int id = startGold; id <= endGold; id++, currentCard = iterator.hasNext() ? iterator.next() : currentCard){

            GoldCard goldTest = (GoldCard) CardBuilder.buildCard(id).frontSide();
            goldTest.setOwner(playerTest);
            JsonNode cardJson = CardBuilder.getCardJson(id, "placeableCards");
            String bonusType = CardBuilder.getBonusType(cardJson);
            int nativePoints = CardBuilder.getPoints(cardJson);

            switch(bonusType){
                case "OBJECT":
                    playerTest.placeCard(goldTest, currentCard.getValidCorners().getFirst());
                    Content bonusContent = CardBuilder.getBonusContent(cardJson);
                    int expectedPoints = playerTest.getPlayerContent().get(bonusContent) * nativePoints;
                    assertEquals(expectedPoints, goldTest.getPoints());
                    break;
                case "CORNER":
                    if(playerTest.checkRequirements(goldTest) &&
                            playerTest.checkIfPlaceable(currentCard.corners.stream().filter(c -> c.getLocation() == Location.TR).findFirst().orElseThrow())){

                        playerTest.placeCard(goldTest, currentCard.corners.stream().filter(c -> c.getLocation() == Location.TR).findFirst().orElseThrow());
                        assertEquals(2 * nativePoints, goldTest.getPoints());
                        break;
                    }
                    playerTest.placeCard(goldTest, currentCard.getValidCorners().getFirst());
                    assertEquals(nativePoints, goldTest.getPoints());
                    break;
                case "NOTHING":
                    assertEquals(nativePoints, goldTest.getPoints());
            }
        }
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