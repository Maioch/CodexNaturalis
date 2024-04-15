package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.model.card.BasicCard;
import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.deck.TurnDeck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EmptyStackException;
import java.util.HashMap;

public class PlayerTest {
    private final ArrayList<String> nicknames = new ArrayList<String>(){{ add("test"); add("test2"); }};
    private final ArrayList<Content> colors = new ArrayList<Content>(){{ add(Content.RED); }};

    @Test
    public void equalsTest(){
                Player referencePlayer = new Player(nicknames.get(0),
                        colors.get(0),
                        new ArrayList<>(Arrays.asList(
                                CardBuilder.buildCard(32),
                                CardBuilder.buildCard(45),
                                CardBuilder.buildCard(67))),
                        new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                                CardBuilder.buildObjective(91))));
        Player testEqualPlayer = new Player(nicknames.get(92),
                colors.get(0),
                new ArrayList<>(Arrays.asList(CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(67))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))));
        Player testDifferentPlayer = new Player(nicknames.get(1),
                colors.get(0),
                new ArrayList<>(Arrays.asList(CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(67))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))));
        assertEquals(testEqualPlayer,referencePlayer);
        assertNotEquals(testDifferentPlayer,referencePlayer);
    }

    @Test
    public void getPlayerContentTest(){
        ArrayList<CardSides> cardsForHand = new ArrayList<>(){{
            for(int i = 1; i <= 80; i++){
                add(CardBuilder.buildCard(i));
            }
        }};
        Player playerTest = new Player(nicknames.get(0), colors.get(0), new ArrayList<>(cardsForHand), new ArrayList<>());
        HashMap<Content, Integer> expectedResult = new HashMap<>(){{
            for(Content content : Content.values()) {
                put(content, 0);
                for (CardSides card : cardsForHand) {
                    compute(content, (k, currentCount) -> currentCount + card.frontSide().getCardSymbols().get(content));
                }
            }
        }};
        int coordinates = 0;
        for(BasicCard card : cardsForHand.stream().map(c -> c.frontSide()).toList()){
            Corner corner = new Corner(Content.WHITE, Location.TR);
            corner.setX(coordinates);
            corner.setY(coordinates);
            playerTest.placeCard(card, corner);
            coordinates += 10;
        }
        assertEquals(expectedResult, playerTest.getPlayerContent());
    }

    @Test
    public void getObjectivePointsTest() {

    }

    /**
     * verify the functionality of checkIfPlaceable. this method creates its own cards
     * to avoid
     */
    @Test
    public void checkIfPlaceableTest(){
        Player referencePlayer = new Player(nicknames.get(0),
                colors.get(0),
                new ArrayList<>(Arrays.asList(CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(67))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))));
        BasicCard starterCard = CardBuilder.buildCard(82).frontSide();
        //Test placing a resource card on a valid corner
        BasicCard firstCard = referencePlayer.getHandCards().get(0).frontSide();
        referencePlayer.placeStarterCard(starterCard);
        Corner cornerToPlaceOn =starterCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        boolean canBePlaced = referencePlayer.checkIfPlaceable(firstCard, cornerToPlaceOn);
        referencePlayer.placeCard(firstCard, cornerToPlaceOn);
        assertEquals(canBePlaced, referencePlayer.getPlacedCards().contains(firstCard));
        //Test placing a gold card without the required resources on a valid corner
        BasicCard secondCard = referencePlayer.getHandCards().get(1).frontSide();
        cornerToPlaceOn = firstCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.BR)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkIfPlaceable(secondCard, cornerToPlaceOn);
        referencePlayer.placeCard(secondCard, cornerToPlaceOn);
        assertEquals(canBePlaced, referencePlayer.getPlacedCards().contains(secondCard));
        //Test placing a gold card with the required resources on a valid corner
        BasicCard thirdCard = referencePlayer.getHandCards().get(2).frontSide();
        cornerToPlaceOn = firstCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.B)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkIfPlaceable(thirdCard, cornerToPlaceOn);
        referencePlayer.placeCard(thirdCard, cornerToPlaceOn);
        assertEquals(canBePlaced, referencePlayer.getPlacedCards().contains(secondCard));

    }

    @Test
    public void placeCardTest(){
        ArrayList<CardSides> cardsForHand = new ArrayList<>(){{
            for(int i = 1; i <= 80; i++){
                add(CardBuilder.buildCard(i));
            }
        }};
        Player playerTest = new Player(nicknames.get(0), colors.get(0), new ArrayList<>(cardsForHand), new ArrayList<>());
        Corner cornerTest = new Corner(Content.WHITE, Location.BL);
        boolean front = true;
        for(CardSides card : cardsForHand){
            BasicCard currentCard = front ? card.frontSide() : card.backSide();
            playerTest.placeCard(currentCard, cornerTest);
            front = !front;
            if(!playerTest.checkIfPlaceable(currentCard, cornerTest)) {
                assertTrue(playerTest.getHandCards().contains(card));
                assertFalse(playerTest.getPlacedCards().contains(currentCard));
            } else {
                assertFalse(playerTest.getHandCards().contains(card));
                assertTrue(playerTest.getPlacedCards().contains(currentCard));
                assertEquals(cornerTest.getX(), currentCard.getAllCorners().stream()
                        .filter(c -> c.getLocation() == cornerTest.getLocation())
                        .findFirst().orElseThrow().getX());
                assertEquals(cornerTest.getY(), currentCard.getAllCorners().stream()
                        .filter(c -> c.getLocation() == cornerTest.getLocation())
                        .findFirst().orElseThrow().getY());
                assertFalse(cornerTest.getVisibility());
            }
        }
    }

    @Test
    public void drawCardTest(){
        int numOfVisibleCards = 2;
        TurnDeck deckTest = new TurnDeck(1, 80, numOfVisibleCards);
        Player playerTest = new Player(nicknames.get(0), colors.get(0), new ArrayList<>(), new ArrayList<>());
        try {
            int index = 0;
            while (!deckTest.isEmpty()) {
                CardSides card = (index == 0) ? deckTest.getCardOnTop() : deckTest.getVisibleCards().get(index - 1);
                playerTest.drawCard(deckTest, index);
                assertTrue(playerTest.getHandCards().contains(card));
                index = (index + 1) % (numOfVisibleCards + 1);
            }
        }catch(EmptyStackException e){
            while(!deckTest.isEmpty()){
                CardSides card = deckTest.getVisibleCards().get(0);
                playerTest.drawCard(deckTest, 0);
                assertTrue(playerTest.getHandCards().contains(card));
            }
        }
    }
}