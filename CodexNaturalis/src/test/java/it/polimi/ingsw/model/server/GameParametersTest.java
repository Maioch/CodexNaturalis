package it.polimi.ingsw.model.server;

import it.polimi.ingsw.model.server.card.CardType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class GameParametersTest {

    @Test
    public void getStartCardIndexTest() throws RuntimeException {
        int result1 = GameParameters.getStartCardIndex(CardType.RESOURCE);
        int result2 = GameParameters.getStartCardIndex(CardType.GOLD);
        int result3 = GameParameters.getStartCardIndex(CardType.STARTER);
        int result4 = GameParameters.getStartCardIndex(CardType.OBJECTIVE);

        assertEquals(result1, 1);
        assertEquals(result2, 41);
        assertEquals(result3, 81);
        assertEquals(result4, 87);
    }

    @Test
    public void getEndCardIndexTest() throws RuntimeException {
        int result1 = GameParameters.getEndCardIndex(CardType.RESOURCE);
        int result2 = GameParameters.getEndCardIndex(CardType.GOLD);
        int result3 = GameParameters.getEndCardIndex(CardType.STARTER);
        int result4 = GameParameters.getEndCardIndex(CardType.OBJECTIVE);

        assertEquals(result1, 40);
        assertEquals(result2, 80);
        assertEquals(result3, 86);
        assertEquals(result4, 102);
    }

    @Test
    public void getNumberOfVisibleCardsTest() throws RuntimeException {
        assertEquals(GameParameters.getNumberOfVisibleCards(), 2);
    }

    @Test
    public void getNumberOfGoldCardsInHandTest() throws RuntimeException {
        assertEquals(GameParameters.getNumberOfGoldCardsInHand(), 1);
    }

    @Test
    public void getNumberOfResourceCardsInHandTest() throws RuntimeException {
        assertEquals(GameParameters.getNumberOfResourceCardsInHand(), 2);
    }

    @Test
    public void getNumberOfSecretObjectivesTest() throws RuntimeException {
        assertEquals(GameParameters.getNumberOfSecretObjectives(), 1);
    }

    @Test
    public void getNumberOfDrawnSecretObjectivesTest() throws RuntimeException {
        assertEquals(GameParameters.getNumberOfDrawnSecretObjectives(), 2);
    }

    @Test
    public void getForfeitTimeTest() throws RuntimeException {
        assertTrue(GameParameters.getForfeitTime() > 0);
    }

    @Test
    public void getMaxPlayersTest() throws RuntimeException{
        assertEquals(GameParameters.getMaxPlayers(), 4);
    }

    @Test
    public void getMinPlayersTest() throws RuntimeException{
        assertEquals(GameParameters.getMinPlayers(), 2);
    }

    @Test
    public void getWinThresholdTest() throws RuntimeException{
        assertEquals(GameParameters.getWinThreshold(), 20);
    }

    @Test
    public void getTCPPortTest() throws RuntimeException{
        assertTrue(GameParameters.getTCPPort() > 0 && GameParameters.getTCPPort() < 65535);
    }

    @Test
    public void getRMIPortTest() throws RuntimeException{
        assertTrue(GameParameters.getRMIPort() > 0 && GameParameters.getRMIPort() < 65535);
    }

    @Test
    public void getMaxNicknameLengthTest() throws RuntimeException{
        assertTrue(GameParameters.getMaxNicknameLength() > 0);
    }

    @Test
    public void getMaxChatMessageLengthTest() throws RuntimeException{
        assertTrue(GameParameters.getMaxChatMessageLength() > 0);
    }

    @Test
    public void getPingPeriodSecondsTest() throws RuntimeException{
        assertTrue(GameParameters.getPingPeriodSeconds() > 0);
    }

    @Test
    public void getLobbyTimeoutTest() throws RuntimeException{
        assertTrue(GameParameters.getLobbyTimeout() > 0);
    }

    @Test
    public void getCommandCharTest() throws RuntimeException{
        String result = GameParameters.getCommandChar();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getDelimiterTest() throws RuntimeException{
        String result = GameParameters.getDelimiter();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getGameHelpBodyTest() throws RuntimeException{
        String result = GameParameters.getGameHelpBody();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getSetupHelpBodyTest() throws RuntimeException{
        String result = GameParameters.getSetupHelpBody();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getRulesURLTest() throws RuntimeException{
        String result = GameParameters.getRulesURL();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }

    @Test
    public void getTitleTest() throws RuntimeException{
        String result = GameParameters.getTitle();
        assertFalse(result.isEmpty());
        assertNotNull(result);
    }
}