package it.polimi.ingsw.model.card;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import it.polimi.ingsw.model.card.BasicCard;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Francesco Nisoli, Guglielmo Gatti
 */
public class BasicCardTest {

    private final ArrayList<Content> contentForTest = new ArrayList<>(){{
        add(Content.RED);
        add(Content.BLUE);
        add(Content.EMPTY);
        add(Content.PEN);
    }
    };
    private final Content colorForTest = Content.BLUE;
    private final HashMap<Location, Corner> corners1 = new HashMap<Location,Corner>(){{
        put(Location.TR, new Corner(0,1, contentForTest.get(0)));
        put(Location.TL, new Corner(1,1, contentForTest.get(1)));
        put(Location.BR, new Corner(0,0, contentForTest.get(2)));
        put(Location.BL, new Corner(1,0, contentForTest.get(3)));
    }
    };

    private final ArrayList<Content> permResourcesForTest = new ArrayList<Content>(Arrays.asList(contentForTest.get(0), contentForTest.get(1)));

    private final BasicCard card1 = new BasicCard(0, colorForTest, corners1, 0, permResourcesForTest);

    @Test
    void getPointsTest(){
        assertEquals(0, card1.getPoints());
    }

    @Test
    void getResourcesTest(){
        for (int i = 0; i < permResourcesForTest.size(); i++) {
            assertEquals(permResourcesForTest.get(i), card1.getResources().get(i));
        }
    }

    @Test
    void getColorTest(){ assertEquals(colorForTest, card1.color); }

    @Test
    void getValidCornersTest(){
        assertTrue(card1.getValidCorners().equals(corners1.values().stream().filter(Corner::getVisibility).toList()));
    }

    /**
     * Method used for testing getCardSymbols() by comparing its return value
     * against the total amount of each content type
     */
    @Test
    public void getCardSymbolsTest(){
        HashMap<Content,Integer> totalSymbols = card1.getCardSymbols();
        for(Content content : contentForTest){
            int numberOfTestContents = countResources(contentForTest, content);
            numberOfTestContents += countResources(permResourcesForTest, content);
            assertEquals(totalSymbols.get(content),numberOfTestContents);
        }
    }

    /**
     * Helper method to quickly count the amount of a type of content inside an arraylist
     * @param contents the ArrayList containing the contents themselves
     * @param toCount the type of content to count
     * @return the amount of the supplied content type in the ArrayList
     */
    private int countResources(ArrayList<Content> contents, Content toCount){
        return contents.stream()
                .filter(x -> x == toCount)
                .mapToInt(x -> 1)
                .reduce(0,Integer::sum);
    }
}
