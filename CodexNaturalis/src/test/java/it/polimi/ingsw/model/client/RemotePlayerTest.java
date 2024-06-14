package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.CardSides;
import org.junit.jupiter.api.Test;

import java.util.List;


import static it.polimi.ingsw.model.client.Utilities.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class RemotePlayerTest {
    @Test
    void handCardsTest(){
        TestView testView = new TestView();
        RemotePlayer player = new RemotePlayer("test", Content.RED);
        player.setViewReferences(testView, new TestSubmitter());
        List<CardSides> cards = List.of(
                CardBuilder.buildCard(10),
                CardBuilder.buildCard(32),
                CardBuilder.buildCard(43));
        player.setHandCards(cards,true);
        var recentCalls = testView.getRecentCalls();
        assertEquals(1, recentCalls.size());
        checkForUpdate(recentCalls,"updateRemotePlayerHand", List.of("test", cards.stream().map(CardSides::backSide).toList()));
        player.setHandCards(cards,false);
        recentCalls = testView.getRecentCalls();
        assertEquals(0, recentCalls.size());
        assertEquals(cards.stream().map(CardSides::backSide).toList(), player.getHandCards());
    }

    @Test
    void equalsTest(){
        RemotePlayer player1 = new RemotePlayer("test", Content.RED);
        RemotePlayer player2 = new RemotePlayer("test", Content.BLUE);
        RemotePlayer player3 = new RemotePlayer("jon", Content.RED);
        assertEquals(player1, player2);
        assertNotEquals(player1, player3);
        //noinspection AssertBetweenInconvertibleTypes
        assertNotEquals(player1, "test");
    }
}