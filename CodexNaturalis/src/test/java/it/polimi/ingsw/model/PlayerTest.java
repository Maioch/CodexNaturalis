package it.polimi.ingsw.model;

import it.polimi.ingsw.model.card.BasicCard;
import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.Objective;
import it.polimi.ingsw.model.card.corner.Corner;
import it.polimi.ingsw.model.card.corner.Location;
import it.polimi.ingsw.model.deck.TurnDeck;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private final ArrayList<String> nicknames = new ArrayList<>(){{ add("test"); add("test2"); }};
    private final ArrayList<Content> colors = new ArrayList<>(){{ add(Content.RED); }};

    @Test
    public void equalsTest(){
        Player referencePlayer = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(Arrays.asList(
                        CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(67))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(91))));
        Player testEqualPlayer = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(Arrays.asList(CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(67))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(91))));
        Player testDifferentPlayer = new Player(nicknames.get(1),
                colors.getFirst(),
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
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(cardsForHand), new ArrayList<>());
        HashMap<Content, Integer> expectedResult = new HashMap<>(){{
            for(Content content : Content.values()) {
                put(content, 0);
                for (CardSides card : cardsForHand) {
                    computeIfPresent(content, (k, currentCount) -> currentCount + card.frontSide().getCardSymbols().get(content));
                }
            }
        }};
        int coordinates = 0;
        for(BasicCard card : cardsForHand.stream().map(CardSides::frontSide).toList()){
            Corner corner = new Corner(Content.WHITE, Location.TR);
            corner.setX(coordinates);
            corner.setY(coordinates);
            playerTest.placeCard(card, corner);
            coordinates += 10;
        }
        assertEquals(expectedResult, playerTest.getPlayerContent());
    }

    /*
     * DA RIVEDERE
     */
    @Test
    public void awardObjectivePointsTest(){
        Objective referenceContentObjective = CardBuilder.buildObjective(95);
        Objective referencePatternObjective = CardBuilder.buildObjective(87);
        Objective referencePatternObjective2 = CardBuilder.buildObjective(91);

        Player playerTest = new Player(
                nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(Arrays.asList(
                        CardBuilder.buildCard(72),
                        CardBuilder.buildCard(73),
                        CardBuilder.buildCard(74)
                )),
                new ArrayList<>(Arrays.asList(
                        referenceContentObjective,
                        referencePatternObjective,
                        referencePatternObjective2
                ))
        );
        LinkedHashMap<Integer,Location> placements = new LinkedHashMap<>(){{
            put(2,Location.TL);
            put(4,Location.TR);
            //put(47,Location.TR);
            put(9,Location.BR);
            put(11,Location.BR);
            //put(1,Location.BL);
        }};

        BasicCard starter = CardBuilder.buildCard(83).frontSide();
        starter.setOwner(playerTest);
        playerTest.placeStarterCard(starter);

        TestUtilities.createTestBoard(playerTest, placements, starter,false);

        //playerTest.placeCard(CardBuilder.buildCard(47), playerTest.getPlacedCards());

        assertEquals(2, playerTest.awardObjectivePoints().get(0));
        assertEquals(2, playerTest.awardObjectivePoints().get(1));
        assertEquals(3, playerTest.awardObjectivePoints().get(2));
    }

    /**
     * verify the functionality of checkIfPlaceable and checkRequirements. this method creates its own cards
     * to avoid
     */
    @Test
    public void checkIfCardIsPlaceableTest(){
        Player referencePlayer = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(Arrays.asList(CardBuilder.buildCard(32),
                        CardBuilder.buildCard(45),
                        CardBuilder.buildCard(73))),
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))));
        BasicCard starterCard = CardBuilder.buildCard(82).frontSide();
        referencePlayer.placeStarterCard(starterCard);

        //Test placing a resource card on a valid corner
        BasicCard firstCard = referencePlayer.getHandCards().getFirst().frontSide();
        Corner cornerToPlaceOn = starterCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        boolean canBePlaced = referencePlayer.checkRequirements(firstCard) && referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        referencePlayer.placeCard(firstCard, cornerToPlaceOn);
        assertTrue(canBePlaced);

        //Test placing a gold card without the required resources on a valid corner
        BasicCard secondCard = referencePlayer.getHandCards().getFirst().frontSide();
        cornerToPlaceOn = firstCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.BR)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkRequirements(secondCard) && referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        assertFalse(canBePlaced);

        //Test placing a gold card with the required resources on a valid corner
        BasicCard thirdCard = referencePlayer.getHandCards().get(1).frontSide();
        cornerToPlaceOn = firstCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkRequirements(thirdCard) && referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        referencePlayer.placeCard(thirdCard, cornerToPlaceOn);
        assertTrue(canBePlaced);

        //Test placing a resource card on an already occupied corner
        referencePlayer.addCardToHand(CardBuilder.buildCard(9));
        BasicCard fourthCard = referencePlayer.getHandCards().getFirst().frontSide();
        cornerToPlaceOn = firstCard.getAllCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkRequirements(fourthCard) && referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        assertFalse(canBePlaced);
    }

    @Test
    public void placeCardTest(){
        ArrayList<CardSides> cardsForHand = new ArrayList<>(){{
            for(int i = 1; i <= 80; i++){
                add(CardBuilder.buildCard(i));
            }
        }};
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(cardsForHand), new ArrayList<>());
        boolean front = true;
        int coordinates = 0;
        for(CardSides card : cardsForHand){
            Corner cornerTest = new Corner(Content.WHITE, Location.TR);
            cornerTest.setX(coordinates);
            cornerTest.setY(coordinates);
            BasicCard currentCard = front ? card.frontSide() : card.backSide();
            playerTest.placeCard(currentCard, cornerTest);
            front = !front;
            if(!playerTest.checkRequirements(currentCard)) {
                assertTrue(playerTest.getHandCards().contains(card));
                assertFalse(playerTest.getPlacedCards().contains(currentCard));
            } else {
                assertFalse(playerTest.getHandCards().contains(card));
                assertTrue(playerTest.getPlacedCards().contains(currentCard));
                assertEquals(cornerTest.getX(), currentCard.getAllCorners().stream()
                        .filter(c -> TestUtilities.getOppositeLocation(c.getLocation()) == cornerTest.getLocation())
                        .findFirst().orElseThrow().getX());
                assertEquals(cornerTest.getY(), currentCard.getAllCorners().stream()
                        .filter(c -> TestUtilities.getOppositeLocation(c.getLocation()) == cornerTest.getLocation())
                        .findFirst().orElseThrow().getY());
                assertFalse(cornerTest.getVisibility());
            }
            coordinates += 10;
        }
    }

    @Test
    public void addCardToHandTest(){
        Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>());
        assertTrue(playerTest.getHandCards().isEmpty());
        CardSides card = CardBuilder.buildCard(41);
        playerTest.addCardToHand(card);
        assertTrue(playerTest.getHandCards().contains(card));
    }
}