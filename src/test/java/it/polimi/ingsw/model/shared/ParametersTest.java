package it.polimi.ingsw.model.shared;

import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.CardType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ParametersTest {

    @Test
    public void getStartCardIndexTest() throws RuntimeException {
        int result1 = Parameters.getStartCardIndex(CardType.RESOURCE);
        int result2 = Parameters.getStartCardIndex(CardType.GOLD);
        int result3 = Parameters.getStartCardIndex(CardType.STARTER);
        int result4 = Parameters.getStartCardIndex(CardType.OBJECTIVE);

        assertEquals(result1, 1);
        assertEquals(result2, 41);
        assertEquals(result3, 81);
        assertEquals(result4, 87);
    }

    @Test
    public void getEndCardIndexTest() throws RuntimeException {
        int result1 = Parameters.getEndCardIndex(CardType.RESOURCE);
        int result2 = Parameters.getEndCardIndex(CardType.GOLD);
        int result3 = Parameters.getEndCardIndex(CardType.STARTER);
        int result4 = Parameters.getEndCardIndex(CardType.OBJECTIVE);

        assertEquals(result1, 40);
        assertEquals(result2, 80);
        assertEquals(result3, 86);
        assertEquals(result4, 102);
    }

    @Test
    public void getNumberOfVisibleCardsTest() throws RuntimeException {
        assertEquals(Parameters.getNumberOfVisibleCards(), 2);
    }

    @Test
    public void getNumberOfGoldCardsInHandTest() throws RuntimeException {
        assertEquals(Parameters.getNumberOfGoldCardsInHand(), 1);
    }

    @Test
    public void getNumberOfResourceCardsInHandTest() throws RuntimeException {
        assertEquals(Parameters.getNumberOfResourceCardsInHand(), 2);
    }

    @Test
    public void getNumberOfSecretObjectivesTest() throws RuntimeException {
        assertEquals(Parameters.getNumberOfSecretObjectives(), 1);
    }

    @Test
    public void getNumberOfDrawnSecretObjectivesTest() throws RuntimeException {
        assertEquals(Parameters.getNumberOfDrawnSecretObjectives(), 2);
    }

    @Test
    public void getForfeitTimeTest() throws RuntimeException {
        assertTrue(Parameters.getForfeitTime() > 0);
    }

    @Test
    public void getMaxPlayersTest() throws RuntimeException{
        assertEquals(Parameters.getMaxPlayers(), 4);
    }

    @Test
    public void getMinPlayersTest() throws RuntimeException{
        assertEquals(Parameters.getMinPlayers(), 2);
    }

    @Test
    public void getWinThresholdTest() throws RuntimeException{
        assertEquals(Parameters.getWinThreshold(), 20);
    }

    @Test
    public void getTCPPortTest() throws RuntimeException{
        assertTrue(Parameters.getTCPPort() > 0 && Parameters.getTCPPort() < 65535);
    }

    @Test
    public void getRMIPortTest() throws RuntimeException{
        assertTrue(Parameters.getRMIPort() > 0 && Parameters.getRMIPort() < 65535);
    }

    @Test
    public void getMaxNameLengthTest() throws RuntimeException{
        assertTrue(Parameters.getMaxNameLength() > 0);
    }

    @Test
    public void getMaxChatMessageLengthTest() throws RuntimeException{
        assertTrue(Parameters.getMaxChatMessageLength() > 0);
    }

    @Test
    public void getServerPingPeriodSecondsTest() throws RuntimeException{
        assertTrue(Parameters.getServerPingPeriodSeconds() > 0);
    }

    @Test
    public void getClientPingPeriodSecondsTest() throws RuntimeException{
        assertTrue(Parameters.getClientPingPeriodSeconds() > 0);
    }

    @Test
    public void getLobbyTimeoutTest() throws RuntimeException{
        assertTrue(Parameters.getLobbyTimeout() > 0);
    }

    @Test
    public void getCommandCharTest() throws RuntimeException{
        String result = Parameters.getCommandChar();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getDelimiterTest() throws RuntimeException{
        String result = Parameters.getDelimiter();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getGameHelpBodyTest() throws RuntimeException{
        String result = Parameters.getGameHelpBody();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getSetupHelpBodyTest() throws RuntimeException{
        String result = Parameters.getSetupHelpBody();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getRulesURLTest() throws RuntimeException{
        String result = Parameters.getRulesURL();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getTitleTest() throws RuntimeException{
        String result = Parameters.getTitle();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }
}