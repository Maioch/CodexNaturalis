package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * class used for testing CardBuilder's functionality.
 * The final variables used to construct the reference cards all refer to the
 * first card of that type from the cards.json file, whose id is specified
 * in the testId field.
 * Each method works by comparing a reference card, built
 * using the constructor,to the card with the same id,
 * built by the Card Builder. if they turn out to be equal, the test is considered
 * successful.
 * @author Guglielmo Gatti
 */

public class CardBuilderTest {
    private final ArrayList<Integer> testIds = new ArrayList<>(){{
        add(1);
        add(41);
        add(81);
        add(87);
    }};
    private final ArrayList<Integer> testPoints = new ArrayList<>(){{
        add(0);
        add(1);
        add(0);
        add(2);
    }};
    private final ArrayList<Content> testColors = new ArrayList<>(){{
        add(Content.RED);
        add(Content.RED);
        add(Content.WHITE);
    }};
    private final HashSet<Corner> resourceFrontCorners = new HashSet<>(){{
        add(new Corner(Content.RED, Location.BL));
        add(new Corner(Content.EMPTY, Location.BR));
        add(new Corner(Content.RED, Location.TL));
        add(new Corner(Content.WHITE, Location.TR));
    }};
    private final HashSet<Corner> resourceBackCorners = new HashSet<>(){{
        add(new Corner(Content.WHITE, Location.BL));
        add(new Corner(Content.WHITE, Location.BR));
        add(new Corner(Content.WHITE, Location.TL));
        add(new Corner(Content.WHITE, Location.TR));
    }};
    private final HashSet<Corner> goldFrontCorners = new HashSet<>(){{
        add(new Corner(Content.WHITE, Location.BL));
        add(new Corner(Content.PEN, Location.BR));
        add(new Corner(Content.EMPTY, Location.TL));
        add(new Corner(Content.WHITE, Location.TR));
    }};
    private final HashSet<Corner> starterBackCorners = new HashSet<>(){{
        add(new Corner(Content.PURPLE, Location.BL));
        add(new Corner(Content.BLUE, Location.BR));
        add(new Corner(Content.RED, Location.TL));
        add(new Corner(Content.GREEN, Location.TR));
    }};
    private final HashSet<Corner> starterFrontCorners = new HashSet<>(){{
        add(new Corner(Content.PURPLE, Location.BL));
        add(new Corner(Content.WHITE, Location.BR));
        add(new Corner(Content.WHITE, Location.TL));
        add(new Corner(Content.GREEN, Location.TR));
    }};
    private final ArrayList<Content>goldRequirements = new ArrayList<Content>(){{
        add(Content.RED);
        add(Content.RED);
        add(Content.BLUE);
    }};
    private final ArrayList<Content>starterResources = new ArrayList<Content>(){{
        add(Content.PURPLE);
    }};
    private final HashMap<Point,Content> testPattern = new HashMap<>(){{
        put(new Point(0,0),Content.RED);
        put(new Point(1,1),Content.RED);
        put(new Point(2,2),Content.RED);
    }};

    /**
     * Test whether CardBuilder is creating resource cards correctly
     * by creating the same card through the constructor and through cardBuilder
     * and comparing the results with each other.
     */
    @Test
    public void BuildResourceCard(){
        CardSides testCardSides = CardBuilder.buildCard(testIds.get(0));
        BasicCard testFront = testCardSides.frontSide();
        BasicCard testBack = testCardSides.backSide();
        BasicCard correctFront = new BasicCard(testIds.get(0),
                testColors.get(0), resourceFrontCorners, testPoints.get(0),
                new ArrayList<>());
        BasicCard correctBack = new BasicCard(testIds.get(0),
                testColors.get(0), resourceBackCorners, 0,
                new ArrayList<>(){{add(testColors.get(0));}});
        assertEquals(correctBack,testBack);
        assertEquals(correctFront,testFront);
    }

    /**
     * Test whether CardBuilder is creating resource cards correctly
     */
    @Test
    public void BuildGoldCard(){
        CardSides testCardSides = CardBuilder.buildCard(testIds.get(1));
        BasicCard testFront = testCardSides.frontSide();
        BasicCard testBack = testCardSides.backSide();
        BasicCard basicFront = new BasicCard(testIds.get(1),
                testColors.get(1), goldFrontCorners, testPoints.get(1),
                new ArrayList<>());
        BasicCard correctBack = new BasicCard(testIds.get(1),
                testColors.get(1), resourceBackCorners, 0,
                new ArrayList<>(){{add(testColors.get(1));}});
        GoldCard correctFront = new GoldCard(basicFront,goldRequirements);
        correctFront.setBonus(correctFront.new ObjectBonus(Content.PEN));
        assertEquals(correctBack,testBack);
        assertEquals(correctFront,testFront);
    }

    /**
     * Test whether starter cards are built correctly
     */
    @Test
    public void BuildStarterCard(){
        CardSides testCardSides = CardBuilder.buildCard(testIds.get(2));
        BasicCard testFront = testCardSides.frontSide();
        BasicCard testBack = testCardSides.backSide();
        BasicCard correctFront = new BasicCard(testIds.get(2),
                testColors.get(2), starterFrontCorners, testPoints.get(2),
                starterResources);
        BasicCard correctBack = new BasicCard(testIds.get(2),
                testColors.get(2), starterBackCorners, testPoints.get(2),
                new ArrayList<>());
        assertEquals(correctBack,testBack);
        assertEquals(correctFront,testFront);
    }

    /**
     * Test whether objective cards are built correctly
     */
    @Test
    public void BuildObjectiveCard(){
        Objective objective = CardBuilder.buildObjective(testIds.get(3));
        Objective correctObjective = new Objective(testIds.get(3),testPoints.get(3));
        correctObjective.setBonus(correctObjective.new AlternativePatternBonus(testPattern));
        assertEquals(objective,correctObjective);
    }
}