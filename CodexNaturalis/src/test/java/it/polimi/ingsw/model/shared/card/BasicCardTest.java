package it.polimi.ingsw.model.shared.card;


import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

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
            JsonNode node = CardBuilder.getCardJson(id);
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
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            Content color = CardBuilder.getColor(node);
            assertEquals(color, card.getColor());
        }
    }

    @Test
    void getCornerTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            Set<Corner> actualCorners = CardBuilder.getCorners(node, "cornersFront");
            for(Location loc : Location.values()){
                Corner corner = actualCorners.stream()
                        .filter((c) -> c.getLocation() == loc)
                        .findFirst().orElse(null);
                assertEquals(corner, card.getCorner(loc));
            }
        }
    }

    @Test
    void getCardSymbolsTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            List<Content> resources = (id >= startStarter) ?
                    CardBuilder.getContentFromArray(node, "resources") :
                    new ArrayList<>();
            Map<Content, Integer> actualSymbols = getCorrectSymbols(resources, CardBuilder.getCorners(node, "cornersFront"));
            assertEquals(actualSymbols, card.getCardSymbols());

            for(Corner corner : card.getAllCorners()){
                Content cornerContent = corner.getContent();
                actualSymbols.computeIfPresent(cornerContent, (k, current) -> current - 1);
                card.coverCornerIfPresent(corner);
                assertEquals(actualSymbols, card.getCardSymbols());
            }
        }
    }
    private Map<Content, Integer> getCorrectSymbols(List<Content> permResources, Set<Corner> corners){
        return new HashMap<>(){{
            for(Content content : Content.values())
                put(content, 0);
            for(Content content : permResources){
                this.put(content, this.get(content) + 1);
            }
            for(Corner corner : corners)
                if(corner.getVisibility()){
                    Content content = corner.getContent();
                    this.put(content, this.get(content) + 1);
                }
        }};
    }

    @Test
    void getValidCornersTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            Set<Corner> allCorners = CardBuilder.getCorners(node, "cornersFront");
            ArrayList<Corner> actualCorners = allCorners.stream().
                    filter(Corner::getVisibility).
                    filter(c -> !c.getContent().isEmpty()).
                    collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            assertEquals(
                    actualCorners.stream().
                            sorted(Comparator.comparingInt(a -> a.getContent().ordinal())).
                            collect(Collectors.toList()),
                    card.getAllCorners().stream()
                            .filter(c -> c.getVisibility() && c.getContent() != Content.EMPTY)
                            .sorted(Comparator.comparingInt(a -> a.getContent().ordinal()))
                            .collect(Collectors.toList()));
        }
    }

    @Test
    void getAllCornersTest(){
        for(int id = startResource; id <= endStarter; id++){
            JsonNode node = CardBuilder.getCardJson(id);
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            Set<Corner> actualCorners = CardBuilder.getCorners(node, "cornersFront");
            assertEquals(actualCorners, card.getAllCorners());
        }
    }

    @Test
    void coverCornerIfPresentTest(){
        for(int id = startResource; id <= endStarter; id++) {
            BasicCard card = CardBuilder.buildCard(id).frontSide();
            for (Corner corner : card.getAllCorners()) {
                card.coverCornerIfPresent(corner);
                assertTrue(corner.getVisibility());
                //the corner returned by getAllCorners is a clone
                corner.coverCorner();
                assertFalse(card.getAllCorners().stream().
                        filter(c -> c.equals(corner)).
                        toList().getFirst().getVisibility());
            }
        }
    }

    @Test
    void placeTest(){
        int x = 5, y = 5;
        int offX, offY;
        int i;
        for(Location location : Location.values()) {
            int baseOffsetX = location == Location.TR || location == Location.BR ? 0 : 1;
            int baseOffsetY = location == Location.TR || location == Location.TL ? 0 : 1;
            Corner corner = new Corner(Content.BLUE, location);
            corner.setX(x);
            corner.setY(y);
            for (int id = startResource; id <= endStarter; id++) {
                BasicCard card = CardBuilder.buildCard(id).frontSide();
                card.place(corner);
                i = 0;
                for (Location loc : Location.values()) {
                    offX = i % 2 - baseOffsetX;
                    offY = (i / 2) % 2 - baseOffsetY;
                    assertEquals(x + offX, card.getAllCorners().stream()
                            .filter(c -> c.getLocation() == loc)
                            .findAny()
                            .orElseThrow()
                            .getX());
                    assertEquals(y + offY, card.getAllCorners().stream()
                            .filter(c -> c.getLocation() == loc)
                            .findAny()
                            .orElseThrow()
                            .getY());
                    i++;
                }
            }
        }
    }

    @Test
    void getAllResourcesTest(){
        for(int id = startResource; id <= GameParameters.getEndCardIndex(CardType.GOLD); id++){
            CardSides card = CardBuilder.buildCard(id);
            List<Content> expectedBackResources = new ArrayList<>();
            JsonNode node = CardBuilder.getCardJson(id);
            Content cardColor = CardBuilder.getColor(node);
            expectedBackResources.add(cardColor);
            assertTrue(card.frontSide().getResources().isEmpty());
            assertEquals(card.backSide().getResources(), expectedBackResources);
        }
        for(int id = GameParameters.getEndCardIndex(CardType.STARTER); id <= endStarter; id++){
            CardSides card = CardBuilder.buildCard(id);
            List<Content> expectedFrontResources = new ArrayList<>();
            JsonNode node = CardBuilder.getCardJson(id);
            try {
                List<Content> contentList = CardBuilder.getContentFromArray(node, "resources");
                expectedFrontResources.addAll(contentList);
            } catch (NullPointerException n){
                fail();
            }
            assertEquals(card.frontSide().getResources(), expectedFrontResources);
            assertTrue(card.backSide().getResources().isEmpty());
        }

    }

    @Test
    void equalsTest(){
        BasicCard otherFront = null;
        BasicCard otherBack = null;
        for(int id = startResource; id <= endStarter; id++){
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
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(otherFront, "Test");
    }

    @Test
    void wrongCardTest(){
        assertThrows(CardException.class, () -> new BasicCard(1, Content.EMPTY, new HashSet<>(){{
            for(Location loc : Location.values()) {
                add(new Corner(Content.WHITE, loc));
            }
        }}, 0, new ArrayList<>(), false));
        assertThrows(CardException.class, () -> new BasicCard(1, Content.RED, new HashSet<>(){{
            for(Location loc : Location.values()) {
                add(new Corner(Content.WHITE, loc));
            }
        }}, -1, new ArrayList<>(), false));
        assertThrows(CardException.class, () -> new BasicCard(1, Content.RED, new HashSet<>(){{
            add(new Corner(Content.WHITE, Location.BL));
        }}, 0, new ArrayList<>(), false));
        assertThrows(CardException.class, () -> new BasicCard(1, Content.RED, new HashSet<>(){{
            for(Location loc : Location.values()) {
                add(new Corner(Content.WHITE, loc));
            }
            add(new Corner(Content.RED, Location.BL));
        }}, 0, new ArrayList<>(), false));
    }
}