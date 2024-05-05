package it.polimi.ingsw.model.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.server.card.CardType;

import java.io.File;
import java.io.IOException;

/**
 * Class with static methods used to retrieve the game parameters from a json file
 *
 * @author Andrea Fidanza, Guglielmo Gatti
 */
public class GameParameters {
    private final static String filePath = "resources/";
    private final static String fileName = "parameters.json";

    /**
     * Gets the start and end indexes of the specified card type
     * @param type the card type
     * @return the id of the first card of the requested type
     */
    public static int getStartCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "CardStartIndex");
    }

    /**
     * Gets the end indexes of the specified card type
     * @param type the card type
     * @return the id of the last card of the requested type
     */
    public static int getEndCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "CardEndIndex");
    }

    /**
     * @return the number of visible cards
     */
    public static int getNumberOfVisibleCards(){
        return getParameter("numberOfVisibleCards");
    }

    /**
     * @return number of hand cards from the json file
     */
    public static int getNumberOfGoldCardsInHand(){
        return getParameter("numberOfGoldCardsInHand");
    }

    /**
     * @return number of hand cards from the json file
     */
    public static int getNumberOfResourceCardsInHand(){
        return getParameter("numberOfResourceCardsInHand");
    }

    /**
     * @return number of secret objectives from the json file
     */
    public static int getNumberOfSecretObjectives(){
        return getParameter("numberOfSecretObjectives");
    }

    /**
     * @return number of common objectives from the json file
     */
    public static int getNumberOfCommonObjectives(){
        return getParameter("numberOfCommonObjectives");
    }

    /**
     * @return maximum number of players from the json file
     */
    public static int getMaxPlayers(){
        return getParameter("maxNumberOfPlayers");
    }

    /**
     * @return minimum number of players from the json file
     */
    public static int getMinPlayers(){
        return getParameter("minNumberOfPlayers");
    }

    /**
     * @return the points threshold for the last turn condition from the json file
     */
    public static int getWinThreshold(){
        return getParameter("winThreshold");
    }

    /**
     * @return the port associated to the TCP connections from the json file
     */
    public static int getTCPPort() { return getParameter("TCPPort"); }

    /**
     * @return the port associated to the RMI connections from the json file
     */
    public static int getRMIPort() { return getParameter("RMIPort"); }

    public static int getMaxNicknameLength() { return getParameter("MaxNicknameLength"); }

    public static int getMaxChatMessageLength() { return getParameter("MaxChatMessageLength"); }

    /**
     * Gets the specified parameter read on the json file
     * @param parameter the parameter to retrieve from the file
     * @return the integer corresponding with the given parameter name
     */
    private static int getParameter(String parameter){
        File parametersJson = new File(filePath + fileName);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(parametersJson).get(parameter);
            return node.asInt();
        }
        catch(IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}