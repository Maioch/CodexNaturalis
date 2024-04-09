package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GoldCardTest {
    private final ArrayList<Content> contentForTest = new ArrayList<>(){{
        add(Content.RED);
        add(Content.BLUE);
        add(Content.EMPTY);
        add(Content.PEN);
    }};
    private final ArrayList<Content> requirementsForTest = new ArrayList<>(){{
        add(Content.RED);
        add(Content.BLUE);
        add(Content.EMPTY);
        add(Content.PEN);
    }};
    private final Content colorForTest = Content.BLUE;
    private final HashMap<Location, Corner> cornersForTest = new HashMap<>(){{
        put(Location.TR, new Corner(contentForTest.get(0)));
        put(Location.TL, new Corner(contentForTest.get(1)));
        put(Location.BR, new Corner(contentForTest.get(2)));
        put(Location.BL, new Corner(contentForTest.get(3)));
    }};

    private final BasicCard BasicCardForTest = new BasicCard(0, colorForTest, cornersForTest, 0, contentForTest);
    private final GoldCard card1 = new GoldCard(BasicCardForTest, requirementsForTest);

    @Test
    void getRequirementsTest(){
        for (int i = 0; i < requirementsForTest.size(); i++) {
            assertEquals(requirementsForTest.get(i), card1.getRequirements().get(i));
        }
    }

    @Test
    void getPointsTest(){
        assertEquals(0, card1.getPoints());
    }
}