package it.polimi.ingsw.model;

import it.polimi.ingsw.exceptions.PlayerException;
import it.polimi.ingsw.model.card.*;
import it.polimi.ingsw.model.card.corner.Corner;
import it.polimi.ingsw.model.card.corner.Location;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {
    private final ArrayList<String> nicknames = new ArrayList<>(){{ add("test"); add("test2"); }};
    private final ArrayList<Content> colors = new ArrayList<>(){{ add(Content.RED); }};

    /**
     * A simple test method that checks if the equals method can correctly determine when different instances of Player
     * are actually equal or not
     */
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

    /**
     * Tests whether the content contained in the player's board are computed correctly or not
     */
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

    /**
     * A testing method that checks if the points given in the array are correspondingly correct (objective by objective)
     */
    @Test
    public void awardObjectivePointsTest(){
        Objective referenceContentObjective = CardBuilder.buildObjective(95);
        Objective referenceContentObjective2 = CardBuilder.buildObjective(96);
        Objective referenceContentObjective3 = CardBuilder.buildObjective(97);
        Objective referenceContentObjective4 = CardBuilder.buildObjective(98);

        Player playerTest = new Player(
                nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList(
                        referenceContentObjective,
                        referenceContentObjective2,
                        referenceContentObjective3,
                        referenceContentObjective4
                ))
        );

        Corner fakeCorner = new Corner(Content.WHITE, Location.TR);

        for(int i = GameParameters.getStartCardIndex(CardType.RESOURCE); i <= GameParameters.getEndCardIndex(CardType.GOLD); i++){
            playerTest.placeCard(CardBuilder.buildCard(i).backSide(), fakeCorner);
        }

        assertEquals(12, playerTest.awardObjectivePoints().get(0));
        assertEquals(12, playerTest.awardObjectivePoints().get(1));
        assertEquals(12, playerTest.awardObjectivePoints().get(2));
        assertEquals(12, playerTest.awardObjectivePoints().get(3));
    }

    /**
     * Verify the functionality of checkRequirements by checking that by placing all the red resource card, all gold cards that
     * require only red resources are placeable.
     */
    @Test
    public void checkRequirementsTest(){
        Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>());
        ArrayList<BasicCard> resourceCards = new ArrayList<>(){{
            for(int id = GameParameters.getStartCardIndex(CardType.RESOURCE); id <= GameParameters.getEndCardIndex(CardType.RESOURCE); id++){
                add(CardBuilder.buildCard(id).backSide());
            }
        }};
        ArrayList<BasicCard> goldCards = new ArrayList<>(){{
            for(int id = GameParameters.getStartCardIndex(CardType.GOLD); id <= GameParameters.getEndCardIndex(CardType.GOLD); id++){
                add(CardBuilder.buildCard(id).frontSide());
            }
        }};
        for(BasicCard card : resourceCards){
            assertTrue(playerTest.checkRequirements(card));
        }
        for(BasicCard redCard : resourceCards.stream().filter(c -> c.getColor() == Content.RED).toList()){
            playerTest.placeCard(redCard, new Corner(Content.WHITE, Location.TR));
        }
        for(BasicCard redGold : goldCards.stream().filter(c -> {
            for(Content content : c.getRequirements().keySet()){
                if(content != Content.RED && c.getRequirements().get(content) != 0){
                    return false;
                }
            }
            return true;
        }).toList()){
            assertTrue(playerTest.checkRequirements((redGold)));
        }
        for(BasicCard otherGold : goldCards.stream().filter(c -> c.getRequirements().get(Content.RED) == 0).toList()){
            assertFalse(playerTest.checkRequirements(otherGold));
        }
    }

    /**
     * assert the functionality of checkIfPlaceable by testing it on all the possible conditions
     */
    @Test
    public void checkIfPlaceableTest(){
        Player referencePlayer = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(){{add(CardBuilder.buildCard(32));}},
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))));
        BasicCard starterCard = CardBuilder.buildCard(82).frontSide();
        referencePlayer.placeStarterCard(starterCard);

        //Test checkIfPlaceable on a valid corner
        BasicCard firstCard = referencePlayer.getHandCards().getFirst().frontSide();
        Corner cornerToPlaceOn = starterCard.getValidCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        boolean canBePlaced = referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        referencePlayer.placeCard(firstCard, cornerToPlaceOn);
        assertTrue(canBePlaced);

        //Test checkIfPlaceable on an already covered corner (aka non-visible)
        cornerToPlaceOn = starterCard.getAllCorners().stream()
                .filter(c -> c.getLocation() == Location.TR)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        assertFalse(canBePlaced);

        //Test checkIfPlaceable on a non-empty corner overlapping another card
        cornerToPlaceOn = firstCard.getAllCorners().stream()
                .filter(c -> c.getLocation() == Location.BL)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        assertFalse(canBePlaced);

        //Test checkIfPlaceable on an empty corner
        cornerToPlaceOn = firstCard.getAllCorners().stream()
                .filter(c -> c.getLocation() == Location.TL)
                .findFirst().orElseThrow();
        canBePlaced = referencePlayer.checkIfPlaceable(cornerToPlaceOn);
        assertFalse(canBePlaced);
    }

    /**
     * A testing method that assures the actual placement of a card when certain requirements are satisfied
     */
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

    /**
     * tests if the addCardToHand method is able to add cards to the player's hand
     */
    @Test
    public void addCardToHandTest(){
        Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>());
        assertTrue(playerTest.getHandCards().isEmpty());
        CardSides card = CardBuilder.buildCard(41);
        playerTest.addCardToHand(card);
        assertTrue(playerTest.getHandCards().contains(card));
    }

    /**
     * Tests whether the isCornerPartOfBoard method can correctly assess if a corner is part of the player's board
     */
    @Test
    public void isCornerPartOfBoardTest(){
        Player playerTest = new Player("test", Content.RED,
                new ArrayList<>(){{add(CardBuilder.buildCard(1));}}, new ArrayList<>());
        BasicCard starterCard = CardBuilder.buildCard(81).frontSide();
        playerTest.placeStarterCard(starterCard);
        BasicCard card1 = playerTest.getHandCards().getFirst().frontSide();
        BasicCard card2 = CardBuilder.buildCard(2).frontSide();
        //Test the corners of a card that the player doesn't have
        for(Corner corner : card2.getAllCorners()){
            assertFalse(playerTest.isCornerPartOfBoard(corner));
        }
        //Test the corners of a card that the player has in their hand but which hasn't been placed yet
        for(Corner corner : card1.getAllCorners()){
            assertFalse(playerTest.isCornerPartOfBoard(corner));
        }
        //Test the corners of a card that has already been placed
        playerTest.placeCard(card1,starterCard.getValidCorners().getFirst());
        for(Corner corner : card1.getAllCorners()){
            assertTrue(playerTest.isCornerPartOfBoard(corner));
        }
    }

    /**
     * Tests whether the isCardInHand method can correctly assess if a card is part of the player's hand
     */
    @Test
    public void isCardInHandTest(){
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(), new ArrayList<>());
        Player playerTest2 = new Player(nicknames.get(1), colors.getFirst(), new ArrayList<>(), new ArrayList<>());

        for(int i = GameParameters.getStartCardIndex(CardType.RESOURCE); i <= GameParameters.getEndCardIndex(CardType.RESOURCE); i++) {
            BasicCard cardFront = CardBuilder.buildCard(i).frontSide();
            BasicCard cardBack = CardBuilder.buildCard(i).frontSide();
            CardSides card = new CardSides(cardFront, cardBack);

            playerTest.addCardToHand(card);

            assertTrue(playerTest.isCardInHand(cardFront));
            assertTrue(playerTest.isCardInHand(cardBack));
            assertFalse(playerTest2.isCardInHand(cardFront));
            assertFalse(playerTest2.isCardInHand(cardBack));
        }
    }

    /**
     * Tests if the starter card placement is successful
     */
    @Test
    public void placeStarterCardTest(){
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(), new ArrayList<>());
        BasicCard starterCard = CardBuilder.buildCard(GameParameters.getStartCardIndex(CardType.STARTER)).frontSide();
        assertTrue(playerTest.getPlacedCards().isEmpty());
        playerTest.placeStarterCard(starterCard);
        assertEquals(playerTest.getPlacedCards().size(),1);
        assertTrue(playerTest.getPlacedCards().contains(starterCard));
        assertThrows(PlayerException.class,() -> playerTest.placeStarterCard(starterCard));
    }
}