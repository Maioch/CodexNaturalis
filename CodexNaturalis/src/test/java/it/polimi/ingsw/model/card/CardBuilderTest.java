package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

public class CardBuilderTest {
    private final ArrayList<Integer> testIds = new ArrayList<>(){{
        add(1);
    }};
    private final ArrayList<Integer> testPoints = new ArrayList<>(){{
        add(0);
    }};
    private final ArrayList<Content> testColors = new ArrayList<>(){{
        add(Content.RED);
    }};
    private final ArrayList<Corner> testCorners = new ArrayList<>(){{
        add(new Corner(Content.PEN));
        add(new Corner(Content.EMPTY));
        add(new Corner(Content.RED));
        add(new Corner(Content.WHITE));
    }};

    @Test
    public void testBuildResourceCard(){
        CardSides cardSides = CardBuilder.buildCard(testIds.get(0));
        BasicCard front = cardSides.frontSide();
        BasicCard back = cardSides.backSide();
        assertEquals(front.cardId,testIds.get(0));
        assertEquals(front.getPoints(),testPoints.get(0));
        assertEquals(front.getColor(), testColors.get(0));
        for(Location location : Location.values()){
            assertEquals(testCorners.get(location.ordinal()),front.getAllCorners().get(location));
        }
    }
}