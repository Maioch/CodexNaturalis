package it.polimi.ingsw.model.server;

import it.polimi.ingsw.TestNetworkHandler;
import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.*;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.server.ServerSubject;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class PlayerTest {

    private final ArrayList<String> nicknames = new ArrayList<>(){{ add("test"); add("test2"); }};
    private final ArrayList<Content> colors = new ArrayList<>(){{ add(Content.RED); }};

    @Test
    public void equalsTest(){
        Player referencePlayer = new Player(nicknames.getFirst(), colors.getFirst(),
                new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        Player testEqualPlayer = new Player(nicknames.getFirst(), colors.getFirst(),
                new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        Player testDifferentPlayer = new Player(nicknames.getLast(), colors.getFirst(),
                new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        assertEquals(testEqualPlayer, referencePlayer);
        assertNotEquals(testDifferentPlayer, referencePlayer);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(testEqualPlayer, "Test");
    }

    @Test
    public void getPlayerContentTest(){
        ArrayList<CardSides> cardsForHand = new ArrayList<>(){{
            for(int i = 1; i <= 80; i++){
                add(CardBuilder.buildCard(i));
            }
        }};
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(cardsForHand), new ArrayList<>(), new ServerSubject());
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

    @Test
    public void awardObjectivePointsTest(){
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nicknames.getFirst(),handler1);
        serverSubject.subscribe(nicknames.get(1),handler2);

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
                )),
                serverSubject
        );

        int x = 0;
        int y = 0;

        for(int i = Parameters.getStartCardIndex(CardType.RESOURCE); i <= Parameters.getEndCardIndex(CardType.GOLD); i++){
            Corner fakeCorner = new Corner(Content.WHITE, Location.TR);
            fakeCorner.setX(x);
            fakeCorner.setY(y);
            playerTest.placeCard(CardBuilder.buildCard(i).backSide(), fakeCorner);
            x++;
            y++;
        }

        assertEquals(12, playerTest.awardObjectivePoints().get(0));
        assertEquals(12, playerTest.awardObjectivePoints().get(1));
        assertEquals(12, playerTest.awardObjectivePoints().get(2));
        handler1.getReceivedMessages();
        handler2.getReceivedMessages();
        assertEquals(12, playerTest.awardObjectivePoints().get(3));

        List<Message> received = handler1.getReceivedMessages();
        assertEquals(1, received.size());
        assertEquals(Status.PLAYER_FINAL_SCORE, received.removeFirst().getStatus());
        received = handler2.getReceivedMessages();
        assertEquals(1,received.size());
        assertEquals(Status.PLAYER_FINAL_SCORE, received.removeFirst().getStatus());
    }

    @Test
    void getObjectivesTest(){
        int startObjective = Parameters.getStartCardIndex(CardType.OBJECTIVE);
        int endObjective = Parameters.getEndCardIndex(CardType.OBJECTIVE);
        List<Objective> objectives = new ArrayList<>();
        for(; startObjective <= endObjective; startObjective++){
            objectives.add(CardBuilder.buildObjective(startObjective));
            Player player = new Player("Test", Content.RED, new ArrayList<>(), objectives, new ServerSubject());
            assertEquals(objectives, player.getObjectives());
        }
    }

    @Test
    public void checkRequirementsTest(){
        Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        ArrayList<BasicCard> resourceCards = new ArrayList<>(){{
            for(int id = Parameters.getStartCardIndex(CardType.RESOURCE); id <= Parameters.getEndCardIndex(CardType.RESOURCE); id++){
                add(CardBuilder.buildCard(id).backSide());
            }
        }};
        ArrayList<BasicCard> goldCards = new ArrayList<>(){{
            for(int id = Parameters.getStartCardIndex(CardType.GOLD); id <= Parameters.getEndCardIndex(CardType.GOLD); id++){
                add(CardBuilder.buildCard(id).frontSide());
            }
        }};
        for(BasicCard card : resourceCards){
            assertTrue(playerTest.checkRequirements(card));
        }
        int x = 0;
        int y = 0;
        for(BasicCard redCard : resourceCards.stream().filter(c -> c.getColor() == Content.RED).toList()){
            Corner corner = new Corner(Content.WHITE, Location.TR);
            corner.setX(x);
            corner.setY(y);
            playerTest.placeCard(redCard, corner);
            x++;
            y++;
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

    @Test
    public void checkIfPlaceableTest(){
        Player referencePlayer = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(){{add(CardBuilder.buildCard(32));}},
                new ArrayList<>(Arrays.asList(CardBuilder.buildObjective(90),
                        CardBuilder.buildObjective(92))),
                new ServerSubject());
        BasicCard starterCard = CardBuilder.buildCard(82).frontSide();
        referencePlayer.placeStarterCard(starterCard);

        //Test checkIfPlaceable on a valid corner
        BasicCard firstCard = referencePlayer.getHandCards().getFirst().frontSide();
        Corner cornerToPlaceOn = starterCard.getAllCorners().stream()
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

    @Test
    public void placeCardTest(){
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nicknames.getFirst(),handler1);
        serverSubject.subscribe(nicknames.get(1),handler2);

        ArrayList<CardSides> cardsForHand = new ArrayList<>(){{
            for(int i = 1; i <= Parameters.getEndCardIndex(CardType.GOLD); i++){
                add(CardBuilder.buildCard(i));
            }
        }};
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(cardsForHand), new ArrayList<>(), serverSubject);
        for(int i = Parameters.getStartCardIndex(CardType.GOLD); i <= Parameters.getEndCardIndex(CardType.GOLD); i++){
            Corner cornerTest = new Corner(Content.WHITE, Location.TR);
            CardSides goldCard = CardBuilder.buildCard(i);
            BasicCard cardWithRequirements = goldCard.frontSide();
            playerTest.placeCard(cardWithRequirements, cornerTest);
            assertTrue(playerTest.getHandCards().contains(goldCard));
            assertFalse(playerTest.getPlacedCards().contains(cardWithRequirements));
        }
        playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(cardsForHand), new ArrayList<>(), serverSubject);
        boolean front = true;
        int coordinates = 0;
        for(CardSides card : cardsForHand){
            Corner cornerTest = new Corner(Content.WHITE, Location.TR);
            cornerTest.setX(coordinates);
            cornerTest.setY(coordinates);
            BasicCard currentCard = front ? card.frontSide() : card.backSide();
            playerTest.placeCard(currentCard, cornerTest);

            List<Message> received = handler1.getReceivedMessages();
            assertEquals(3,received.size());
            assertEquals(Status.PLACEMENT_OK, received.removeFirst().getStatus());
            assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());
            assertEquals(Status.PLAYER_HAND_CARDS, received.removeFirst().getStatus());

            received = handler2.getReceivedMessages();
            assertEquals(2, received.size());
            assertEquals(Status.PLACEMENT_OK, received.removeFirst().getStatus());
            assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());

            front = !front;
            if(!playerTest.checkRequirements(currentCard)) {
                assertTrue(playerTest.getHandCards().contains(card));
                assertFalse(playerTest.getPlacedCards().contains(currentCard));
            } else {
                assertFalse(playerTest.getHandCards().contains(card));
                assertTrue(playerTest.getPlacedCards().contains(currentCard));
                assertEquals(cornerTest.getX(), currentCard.getAllCorners().stream()
                        .filter(c -> c.getLocation().getOppositeLocation() == cornerTest.getLocation())
                        .findFirst().orElseThrow().getX());
                assertEquals(cornerTest.getY(), currentCard.getAllCorners().stream()
                        .filter(c -> c.getLocation().getOppositeLocation() == cornerTest.getLocation())
                        .findFirst().orElseThrow().getY());
            }
            coordinates += 10;
        }
    }

    @Test
    public void addPersonalObjectiveTest(){
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nicknames.getFirst(),handler1);
        serverSubject.subscribe(nicknames.get(1),handler2);

        List<Objective> objectives = Arrays.asList(CardBuilder.buildObjective(87), CardBuilder.buildObjective(89));
        Objective personalObjective = CardBuilder.buildObjective(89);
        Player playerTest = new Player(nicknames.getFirst(),
                colors.getFirst(),
                new ArrayList<>(),
                objectives,
                serverSubject);
        assertEquals(playerTest.getObjectives(), objectives);
        playerTest.addPersonalObjectives(List.of(personalObjective));

        List<Message> received = handler1.getReceivedMessages();
        assertEquals(1,received.size());
        assertEquals(Status.SECRET_OBJECTIVES, received.removeFirst().getStatus());

        received = handler2.getReceivedMessages();
        assertTrue(received.isEmpty());

        objectives = new ArrayList<>(objectives);
        objectives.add(personalObjective);
        assertEquals(playerTest.getObjectives(), objectives);
    }

    @Test
    public void addCardToHandTest(){
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nicknames.getFirst(),handler1);
        serverSubject.subscribe(nicknames.get(1),handler2);

        Player playerTest = new Player("test", Content.RED, new ArrayList<>(), new ArrayList<>(), serverSubject);
        assertTrue(playerTest.getHandCards().isEmpty());
        CardSides card = CardBuilder.buildCard(41);
        playerTest.addCardToHand(card);
        assertTrue(playerTest.getHandCards().contains(card));
        List<Message> received = handler1.getReceivedMessages();
        assertEquals(2,received.size());
        assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());
        assertEquals(Status.PLAYER_HAND_CARDS, received.removeFirst().getStatus());
        received = handler2.getReceivedMessages();
        assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());
    }

    @Test
    public void getAllValidCornersTest(){
        Player referencePlayer = new Player(
                "test",
                Content.RED,
                new ArrayList<>(Arrays.asList(
                        CardBuilder.buildCard(72),
                        CardBuilder.buildCard(73),
                        CardBuilder.buildCard(74)
                )),
                new ArrayList<>(),
                new ServerSubject()
        );
        LinkedHashMap<Integer,Location> placements1 = new LinkedHashMap<>(){{
            put(2,Location.TR);
            put(3,Location.TR);
            put(4, Location.TR);
            put(15, Location.TR);
            put(5, Location.TL);
            put(6, Location.BL);
            put(18, Location.TR);
            put(7, Location.BR);
        }};

        BasicCard starter = CardBuilder.buildCard(83).frontSide();
        starter.setOwner(referencePlayer);
        referencePlayer.placeStarterCard(starter);

        BasicCard card = Utilities.createTestBoard(referencePlayer, placements1, starter,true);
        referencePlayer.placeCard(CardBuilder.buildCard(1).frontSide(),
                card.getAllCorners().stream().findFirst().orElseThrow());
        assertEquals(referencePlayer.getPlacedCards().stream()
                .flatMap(b -> b.getAllCorners().stream())
                .filter(referencePlayer::checkIfPlaceable)
                .toList(), referencePlayer.getAllValidCorners());
    }

    @Test
    void getAllValidCardsTest(){
        int startResources = Parameters.getStartCardIndex(CardType.RESOURCE);
        int startGolds = Parameters.getStartCardIndex(CardType.GOLD);
        CardSides resourceCard1 = CardBuilder.buildCard(startResources);
        CardSides resourceCard2 = CardBuilder.buildCard(startResources + 1);
        CardSides goldCard = CardBuilder.buildCard(startGolds);
        Player testPlayer = new Player("Test", Content.RED, new ArrayList<>(Arrays.asList(
                resourceCard1, resourceCard2, goldCard)), new ArrayList<>(), new ServerSubject());
        List<BasicCard> validCards = testPlayer.getAllValidCards();
        List<BasicCard> backSides = new ArrayList<>(Arrays.asList(
                resourceCard1.backSide(), resourceCard2.backSide(), goldCard.backSide()));
        assertTrue(validCards.containsAll(backSides));
        assertTrue(validCards.contains(resourceCard1.frontSide()));
        assertTrue(validCards.contains(resourceCard2.frontSide()));
        assertFalse(validCards.contains(goldCard.frontSide()));
    }

    @Test
    void isPlayerStuckTest(){
        Player playerTest = new Player("test", Content.RED,
                new ArrayList<>(List.of(CardBuilder.buildCard(1))), new ArrayList<>(), new ServerSubject());
        assertTrue(playerTest.isPlayerStuck());
        playerTest.placeCard(CardBuilder.buildCard(1).backSide(), new Corner(Content.WHITE, Location.TR));
        assertFalse(playerTest.isPlayerStuck());
    }

    @Test
    public void isCornerPartOfBoardTest(){
        Player playerTest = new Player("test", Content.RED,
                new ArrayList<>(){{add(CardBuilder.buildCard(1));}}, new ArrayList<>(), new ServerSubject());
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
        playerTest.placeCard(card1,starterCard.getAllCorners().stream()
                .filter(c -> c.getLocation() == Location.TL)
                .findFirst().orElseThrow());
        for(Corner corner : card1.getAllCorners()){
            assertTrue(playerTest.isCornerPartOfBoard(corner));
        }
    }

    @Test
    public void isCardInHandTest(){
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(), new ArrayList<>(), new ArrayList<>(), new ServerSubject());
        Player playerTest2 = new Player(nicknames.get(1), colors.getFirst(), new ArrayList<>(), new ArrayList<>(), new ServerSubject());

        for(int i = Parameters.getStartCardIndex(CardType.RESOURCE); i <= Parameters.getEndCardIndex(CardType.RESOURCE); i++) {
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

    @Test
    public void placeStarterCardTest(){
        TestNetworkHandler handler1 = new TestNetworkHandler();
        TestNetworkHandler handler2 = new TestNetworkHandler();
        ServerSubject serverSubject = new ServerSubject();
        serverSubject.subscribe(nicknames.getFirst(),handler1);
        serverSubject.subscribe(nicknames.get(1),handler2);

        CardSides starterCard = CardBuilder.buildCard(Parameters.getStartCardIndex(CardType.STARTER));
        Player playerTest = new Player(nicknames.getFirst(), colors.getFirst(),
                List.of(starterCard),
                new ArrayList<>(), serverSubject);
        assertTrue(playerTest.getPlacedCards().isEmpty());
        playerTest.placeStarterCard(starterCard.frontSide());
        assertEquals(playerTest.getPlacedCards().size(),1);
        assertTrue(playerTest.getPlacedCards().contains(starterCard.frontSide()));
        assertFalse(playerTest.getHandCards().contains(starterCard));
        Player finalPlayerTest = playerTest;
        assertThrows(CardException.class,() -> finalPlayerTest.placeStarterCard(starterCard.frontSide()));
        playerTest = new Player(nicknames.getFirst(), colors.getFirst(),
                new ArrayList<>(),
                new ArrayList<>(), serverSubject);
        CardSides otherCard = CardBuilder.buildCard(86);
        assertFalse(playerTest.getHandCards().contains(otherCard));
        handler1.getReceivedMessages();
        handler2.getReceivedMessages();
        playerTest.placeStarterCard(otherCard.frontSide());
        assertTrue(playerTest.getPlacedCards().contains(otherCard.frontSide()));
        assertFalse(playerTest.getHandCards().contains(otherCard));

        List<Message> received = handler1.getReceivedMessages();
        assertEquals(3,received.size());
        assertEquals(Status.PLACEMENT_OK, received.removeFirst().getStatus());
        assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());
        assertEquals(Status.PLAYER_HAND_CARDS, received.removeFirst().getStatus());

        received = handler2.getReceivedMessages();
        assertEquals(2, received.size());
        assertEquals(Status.PLACEMENT_OK, received.removeFirst().getStatus());
        assertEquals(Status.PLAYER_HAND_BACK, received.removeFirst().getStatus());
    }
}