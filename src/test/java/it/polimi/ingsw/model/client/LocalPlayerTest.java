package it.polimi.ingsw.model.client;

import it.polimi.ingsw.TestSubmitter;
import it.polimi.ingsw.TestView;
import it.polimi.ingsw.model.shared.Content;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static it.polimi.ingsw.TestView.*;
import static org.junit.jupiter.api.Assertions.*;

public class LocalPlayerTest {

    @Test
    void turnNumberTest(){
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        assertEquals(0, player.getTurnNumber());
        player.setTurnNumber(1);
        assertEquals(1, player.getTurnNumber());
    }

    @Test
    void placedCardsTest(){
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(new TestView(), new TestSubmitter());
        assertTrue(player.getPlacedCards().isEmpty());
        List<BasicCard> cards = List.of(
                CardBuilder.buildCard(10).frontSide(),
                CardBuilder.buildCard(32).frontSide(),
                CardBuilder.buildCard(49).frontSide());
        player.setPlacedCards(cards, 1);
        assertEquals(cards, player.getPlacedCards());
    }

    @Test
    void requestPlacementTest(){
        TestView testView = new TestView();
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(testView, new TestSubmitter());
        player.requestStarterCardPlacement();
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "requestStarterSide", List.of(player.getHandCards()));
        player.requestCardPlacement(new ArrayList<>(), new ArrayList<>());
        recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "requestPlacement", List.of(new ArrayList<>(), new ArrayList<>()));
    }

    @Test
    void personalObjectiveTest(){
        TestView testView = new TestView();
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(testView, new TestSubmitter());
        List<Objective> personalObjectives = List.of(CardBuilder.buildObjective(87),
                CardBuilder.buildObjective(88),
                CardBuilder.buildObjective(89));
        player.setPersonalObjectives(personalObjectives);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls, "showPersonalObjectives", List.of(personalObjectives));
        assertEquals(personalObjectives, player.getPersonalObjectives());
    }

    @Test
    void getValidCardsTest(){
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(new TestView(), new TestSubmitter());
        assertEquals(new ArrayList<>(), player.getValidCards());
        BasicCard card = CardBuilder.buildCard(1).backSide();
        player.requestCardPlacement(List.of(card), new ArrayList<>());
        assertEquals(List.of(card), player.getValidCards());
    }

    @Test
    void getValidCornersTest(){
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(new TestView(), new TestSubmitter());
        assertEquals(new ArrayList<>(), player.getValidCorners());
        BasicCard card = CardBuilder.buildCard(81).backSide();
        player.requestCardPlacement(new ArrayList<>(), card.getAllCorners().stream().toList());
        assertEquals(card.getAllCorners().stream().toList(), player.getValidCorners());
    }

    @Test
    void setFinalScoreTest(){
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        TestView testView = new TestView();
        player.setViewReferences(testView, new TestSubmitter());
        int finalScore = 6;
        Map<Objective,Integer> scoreMap = Map.of(
                CardBuilder.buildObjective(90), 1,
                CardBuilder.buildObjective(91), 2,
                CardBuilder.buildObjective(92), 3);
        player.setFinalScore(scoreMap, finalScore);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1,recentCalls.size());
        checkForUpdate(recentCalls,"revealFinalSummary", List.of("test", scoreMap, finalScore));
    }

    @Test
    void cardHandsTest(){
        TestView testView = new TestView();
        LocalPlayer player = new LocalPlayer("test", Content.RED);
        player.setViewReferences(testView, new TestSubmitter());
        List<CardSides> cards = List.of(
                CardBuilder.buildCard(10),
                CardBuilder.buildCard(32),
                CardBuilder.buildCard(43));
        player.setHandCards(cards,true);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls,"updateLocalPlayerHand", List.of(cards));
        player.setHandCards(cards,false);
        recentCalls = testView.getRecentCalls();
        assertEquals(0, recentCalls.size());
        assertEquals(cards, player.getHandCards());
    }


    @Test
    void equalsTest(){
        LocalPlayer player1 = new LocalPlayer("test", Content.RED);
        LocalPlayer player2 = new LocalPlayer("test", Content.BLUE);
        LocalPlayer player3 = new LocalPlayer("jon", Content.RED);
        assertEquals(player1, player2);
        assertNotEquals(player1, player3);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(player1, "test");
    }
}
