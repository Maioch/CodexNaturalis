package it.polimi.ingsw.model.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.server.card.CardType;

import java.io.IOException;

/**
 * Class with static methods used to retrieve the game parameters from a json file.
 *
 * @author Andrea Fidanza, Guglielmo Gatti
 */
public class GameParameters {
    private final static String filePath = "/gameFiles/";
    private final static String fileName = "parameters.json";

    /**
     * @param type the card type.
     * @return the id of the first card of the requested type.
     */
    public static int getStartCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "CardStartIndex").asInt();
    }

    /**
     * @param type the card type.
     * @return the id of the last card of the requested type.
     */
    public static int getEndCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "CardEndIndex").asInt();
    }

    /**
     * @return the number of visible cards.
     */
    public static int getNumberOfVisibleCards(){
        return getParameter("numberOfVisibleCards").asInt();
    }

    /**
     * @return the number of gold hand cards from the json file.
     */
    public static int getNumberOfGoldCardsInHand(){
        return getParameter("numberOfGoldCardsInHand").asInt();
    }

    /**
     * @return the number of resource hand cards from the json file.
     */
    public static int getNumberOfResourceCardsInHand(){
        return getParameter("numberOfResourceCardsInHand").asInt();
    }

    /**
     * @return the number of secret objectives from the json file.
     */
    public static int getNumberOfSecretObjectives(){
        return getParameter("numberOfSecretObjectives").asInt();
    }

    public static int getNumberOfDrawnSecretObjectives() { return getParameter("numberOfDrawnSecretObjectives").asInt(); }

    public static int getForfeitTime() { return getParameter("forfeitTime").asInt(); }

    /**
     * @return the number of common objectives from the json file.
     */
    public static int getNumberOfCommonObjectives(){
        return getParameter("numberOfCommonObjectives").asInt();
    }

    /**
     * @return the maximum number of players from the json file.
     */
    public static int getMaxPlayers(){
        return getParameter("maxNumberOfPlayers").asInt();
    }

    /**
     * @return the minimum number of players from the json file.
     */
    public static int getMinPlayers(){
        return getParameter("minNumberOfPlayers").asInt();
    }

    /**
     * @return the points threshold required to trigger the last turn from the json file.
     */
    public static int getWinThreshold(){
        return getParameter("winThreshold").asInt();
    }

    /**
     * @return the port associated to the TCP connections from the json file.
     */
    public static int getTCPPort() { return getParameter("TCPPort").asInt(); }

    /**
     * @return the port associated to the RMI connections from the json file.
     */
    public static int getRMIPort() { return getParameter("RMIPort").asInt(); }

    /**
     * @return the maximum length of a nickname from the json file.
     */
    public static int getMaxNicknameLength() { return getParameter("MaxNicknameLength").asInt(); }

    /**
     * @return the maximum length of a chat message from the json file.
     */
    public static int getMaxChatMessageLength() { return getParameter("MaxChatMessageLength").asInt(); }

    /**
     * @return the pinging period used to guarantee clients' connection
     */
    public static int getPingPeriodSeconds(){ return getParameter("PingPeriodSeconds").asInt(); }

    /**
     * @return the prefix used to trigger a command using the CLI version of the game.
     */
    public static String getCommandChar() {
        return getParameter("CommandChar").asText();
    }

    /**
     * @return the char used to separate different arguments of a command.
     */
    public static String getDelimiter() {
        return getParameter("Delimiter").asText();
    }

    /**
     * @return the body of the "/HELP" command during the game phase.
     */
    public static String getGameHelpBody(){
        return getParameter("GameHelpBody").asText();
    }

    /**
     * @return the body of the "/HELP" command during the setup phase.
     */
    public static String getSetupHelpBody() { return getParameter("SetupHelpBody").asText(); }

    /**
     * @return the URL to which players are redirected when they use the "/GETRULES" command.
     */
    public static String getRulesURL() { return getParameter("RulesURL").asText(); }


    public static String getTitle(){
        return getParameter("GameTitle").asText();
    }

    /**
     * Gets the specified parameter read on the json file.
     * @param parameter the parameter to retrieve from the file.
     * @return the integer corresponding with the given parameter name.
     */
    private static JsonNode getParameter(String parameter){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(GameParameters.class.getResource(filePath + fileName)).get(parameter);
            return node;
        }
        catch(IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}