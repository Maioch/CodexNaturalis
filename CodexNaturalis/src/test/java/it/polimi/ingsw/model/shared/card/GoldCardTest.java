package it.polimi.ingsw.model.shared.card;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.Player;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.GoldCard;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GoldCardTest {
    private final int startGold = 41;
    private final int endGold = 80;

    @Test
    void getRequirementsTest(){
        for(int id = startGold; id <= endGold; id++){
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            assertEquals(card.getClass(), GoldCard.class);
            GoldCard gold = (GoldCard) card;
            List<Content> actualRequirements = CardBuilder.getContentFromArray(node, "requirements");
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
        List<BasicCard> placedCard = playerTest.getPlacedCards();
        Iterator<BasicCard> iterator = placedCard.iterator();
        BasicCard currentCard = iterator.next();
        for(int id = startGold; id <= endGold; id++, currentCard = iterator.hasNext() ? iterator.next() : currentCard){

            GoldCard goldTest = (GoldCard) CardBuilder.buildCard(id).frontSide();
            goldTest.setOwner(playerTest);
            JsonNode cardJson = CardBuilder.getCardJson(id);
            String bonusType = CardBuilder.getBonusType(cardJson);
            int nativePoints = CardBuilder.getPoints(cardJson);

            switch(bonusType){
                case "OBJECT":
                    playerTest.placeCard(goldTest, currentCard.getAllCorners().stream()
                            .filter(c -> c.getVisibility() && c.getContent() != Content.EMPTY)
                            .toList()
                            .getFirst());
                    Content bonusContent = CardBuilder.getBonusContent(cardJson);
                    int expectedPoints = playerTest.getPlayerContent().get(bonusContent) * nativePoints;
                    assertEquals(expectedPoints, goldTest.getPoints());
                    break;
                case "CORNER":
                    if(playerTest.checkRequirements(goldTest) &&
                            playerTest.checkIfPlaceable(currentCard.corners.stream()
                                    .filter(c -> c.getLocation() == Location.TR).findFirst().orElseThrow())){
                        playerTest.placeCard(goldTest, currentCard.corners.stream()
                                .filter(c -> c.getLocation() == Location.TR).findFirst().orElseThrow());
                        assertEquals(2 * nativePoints, goldTest.getPoints());
                        break;
                    }
                    playerTest.placeCard(goldTest, currentCard.getAllCorners().stream()
                            .filter(c -> c.getVisibility() && c.getContent() != Content.EMPTY)
                            .toList().getFirst());
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

    @Test
    void wrongCardTest(){
        assertThrows(CardException.class, () ->
                new GoldCard(CardBuilder.buildCard(1).backSide(), new ArrayList<>(List.of(Content.EMPTY))));
    }
}