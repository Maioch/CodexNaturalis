package it.polimi.ingsw.model.card;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Francesco Nisoli, Guglielmo Gatti, Andrea Fidanza
 */
public class BasicCardTest {
    private final int cardId = 0;
    private final int points = 0;
    private final ArrayList<Content> contentForTest = new ArrayList<>(){{
        add(Content.RED);
        add(Content.BLUE);
        add(Content.EMPTY);
        add(Content.PEN);
        add(Content.WHITE);
    }
    };
    private final Content colorForTest = Content.BLUE;
    private final HashMap<Location, Corner> corners1 = new HashMap<>(){{
        put(Location.TR, new Corner(contentForTest.get(0)));
        put(Location.TL, new Corner(contentForTest.get(1)));
        put(Location.BR, new Corner(contentForTest.get(2)));
        put(Location.BL, new Corner(contentForTest.get(3)));
    }
    };
    private final HashMap<Location, Corner> corners2 = new HashMap<>(){{
        put(Location.TR, new Corner(contentForTest.get(1)));
        put(Location.TL, new Corner(contentForTest.get(2)));
        put(Location.BR, new Corner(contentForTest.get(3)));
        put(Location.BL, new Corner(contentForTest.get(4)));
    }
    };

    private final ArrayList<Content> permResourcesForTest = new ArrayList<>(Arrays.asList(contentForTest.get(0), contentForTest.get(1)));

    private final BasicCard card1 = new BasicCard(cardId, colorForTest, corners1, points, permResourcesForTest);
    private final BasicCard card2 = new BasicCard(cardId, colorForTest, corners2, points, permResourcesForTest);

    @Test
    void getPointsTest(){
        assertEquals(points, card1.getPoints());
    }

    @Test
    void getColorTest(){ assertEquals(colorForTest, card1.color); }

    @Test
    void getCardSymbols(){
        HashMap<Content, Integer> correctMap1 = getCorrectSymbols(permResourcesForTest, corners1);
        HashMap<Content, Integer> correctMap2 = getCorrectSymbols(permResourcesForTest, corners2);
        assertEquals(correctMap1, card1.getCardSymbols());
        assertEquals(correctMap2, card2.getCardSymbols());
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
        List<Corner> testResources = corners1.values().stream().filter(Corner::getVisibility).filter(c -> c.getContent() != Content.EMPTY).toList();
        List<Corner> cardResources = card1.getValidCorners();
        assertEquals(corners1.values().stream().filter(Corner::getVisibility).filter(c -> c.getContent() != Content.EMPTY).toList(), card1.getValidCorners());
    }

    @Test
    void getAllCornersTest(){
        assertEquals(corners1, card1.getAllCorners());
    }

    @Test
    void coverCornerTest(){
        BasicCard cardTest = new BasicCard(cardId, colorForTest, corners1, points, permResourcesForTest);
        cardTest.coverCorner(card1.getAllCorners().get(Location.BL));
        assertFalse(card1.getAllCorners().get(Location.BL).getVisibility());
    }

    @Test
    void placeTest(){
        int x = 5, y = 5;
        int offX, offY;
        int i = 0;
        card1.place(x,y);
        for(Location loc : Location.values()) {
            offX = i % 2;
            offY = (i / 2) % 2;
            assertEquals(x + offX, card1.getAllCorners().get(loc).getX());
            assertEquals(y + offY, card1.getAllCorners().get(loc).getY());
            i++;
        }
    }

    @Test
    void equalsTest(){
        assertNotEquals(card1, card2);
        ArrayList<Content> otherResources = new ArrayList<>(Arrays.asList(contentForTest.get(1), contentForTest.get(0)));
        BasicCard other = new BasicCard(cardId, colorForTest, corners1, points, otherResources);
        assertEquals(card1, other);
    }
}