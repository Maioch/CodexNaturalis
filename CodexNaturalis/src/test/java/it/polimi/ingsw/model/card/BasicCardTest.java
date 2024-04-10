package it.polimi.ingsw.model.card;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * @author Francesco Nisoli, Guglielmo Gatti, Andrea Fidanza
 */
public class BasicCardTest {
    private final int startResource = 1;
    private final int endResource = 40;
    private final int startStarter = 81;
    private final int endStarter = 86;

    @Test
    void getPointsTest(){
        for(int id = startResource; id <= endResource; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            int actualPoints = CardBuilder.getPoints(node);
            assertEquals(actualPoints, card.getPoints());
        }
        for(int id = startStarter; id <= endStarter; id++){
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            assertEquals(0, card.getPoints());
        }
    }

    @Test
    void getColorTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            Content color = CardBuilder.getColor(node);
            assertEquals(color, card.getColor());
        }
    }

    @Test
    void getCardSymbols(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            ArrayList<Content> resources = (id >= startStarter) ?
                    CardBuilder.getContentFromArray(node, "resources") :
                    new ArrayList<>();
            HashMap<Content, Integer> actualSymbols = getCorrectSymbols(resources, CardBuilder.getCorners(node, "cornersFront"));
            assertEquals(actualSymbols, card.getCardSymbols());
            for(Location loc : Location.values()){
                Content cornerContent = card.getAllCorners().get(loc).getContent();
                int current = actualSymbols.get(cornerContent);
                actualSymbols.put(cornerContent, current - 1);
                card.coverCorner(card.getAllCorners().get(loc));
                assertEquals(actualSymbols, card.getCardSymbols());
            }
        }
    }
    private HashMap<Content, Integer> getCorrectSymbols(ArrayList<Content> permResources, HashMap<Location, Corner> corners){
        return new HashMap<>(){{
            for(Content content : Content.values())
                put(content, 0);
            for(Content content : permResources){
                this.put(content, this.get(content) + 1);
            }
            for(Corner corner : corners.values())
                if(corner.getVisibility()){
                    Content content = corner.getContent();
                    this.put(content, this.get(content) + 1);
                }
        }};
    }

    @Test
    void getValidCornersTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            HashMap<Location, Corner> allCorners = CardBuilder.getCorners(node, "cornersFront");
            ArrayList<Corner> actualCorners = allCorners.values().stream().
                    filter(Corner::getVisibility).
                    filter(c -> c.getContent() != Content.EMPTY).
                    collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            assertEquals(
                    actualCorners.stream().
                            sorted(Comparator.comparingInt(a -> a.getContent().ordinal())).
                            collect(Collectors.toList()),
                    card.getValidCorners().stream().
                            sorted(Comparator.comparingInt(a -> a.getContent().ordinal())).
                            collect(Collectors.toList()));
        }
    }

    @Test
    void getAllCornersTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id, "placeableCards");
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            HashMap<Location, Corner> actualCorners = CardBuilder.getCorners(node, "cornersFront");
            assertEquals(actualCorners, card.getAllCorners());
        }
    }

    @Test
    void coverCornerTest(){
        for(int id = startResource; id <= endStarter; id++) {
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            for (Location loc : Location.values()) {
                card.coverCorner(card.getAllCorners().get(loc));
                assertFalse(card.getAllCorners().get(loc).getVisibility());
            }
        }
    }

    @Test
    void placeTest(){
        int x = 5, y = 5;
        int offX, offY;
        int i;
        for(int id = startResource; id <= endStarter; id++) {
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            card.place(x, y);
            i = 0;
            for (Location loc : Location.values()) {
                offX = i % 2;
                offY = (i / 2) % 2;
                assertEquals(x + offX, card.getAllCorners().get(loc).getX());
                assertEquals(y + offY, card.getAllCorners().get(loc).getY());
                i++;
            }
        }
    }

    @Test
    void equalsTest(){
        BasicCard otherFront = null;
        BasicCard otherBack = null;
        for(int id = startResource; id <= endStarter; id++){
            CardSides cardSides = CardBuilder.buildCard(id);
            assertNotEquals(cardSides.frontSide(), cardSides.backSide());
            BasicCard front = CardBuilder.buildCard(id).frontSide();
            BasicCard back = CardBuilder.buildCard(id).backSide();
            if(otherFront != null && otherBack != null){
                assertNotEquals(front, otherFront);
                assertNotEquals(back, otherBack);
            }
            BasicCard sameFront = CardBuilder.buildCard(id).frontSide();
            BasicCard sameBack = CardBuilder.buildCard(id).backSide();
            assertEquals(front, sameFront);
            assertEquals(back, sameBack);
            otherFront = front;
            otherBack = back;
            if(id == endResource)
                id = startStarter - 1;
        }
    }
}