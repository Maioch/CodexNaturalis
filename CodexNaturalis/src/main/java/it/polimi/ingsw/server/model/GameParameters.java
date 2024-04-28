package it.polimi.ingsw.server.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.server.model.card.CardType;

import java.io.File;
import java.io.IOException;

/**
 * Class with static methods used to retrieve the game parameters from a json file
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
     * Gets the number of visible cards read on the json file
     * @return the number of visible cards
     */
    public static int getNumberOfVisibleCards(){
        return getParameter("numberOfVisibleCards");
    }

    /**
     * Method that gets the correct number of resource cards allowed given to each player during the first draw
     * @return number of hand cards from the json file
     */
    public static int getNumberOfGoldCardsInHand(){
        return getParameter("numberOfGoldCardsInHand");
    }

    /**
     * Method that gets the correct number of gold cards given to each player during the first draw
     * @return number of hand cards from the json file
     */
    public static int getNumberOfResourceCardsInHand(){
        return getParameter("numberOfResourceCardsInHand");
    }

    /**
     * Method that gets the correct number of secret objectives allowed for each player
     * @return number of secret objectives from the json file
     */
    public static int getNumberOfSecretObjectives(){
        return getParameter("numberOfSecretObjectives");
    }

    /**
     * Method that gets the correct number of common objectives allowed per game
     * @return number of common objectives from the json file
     */
    public static int getNumberOfCommonObjectives(){
        return getParameter("numberOfCommonObjectives");
    }

    /**
     * Method that gets the maximum number of players allowed in a game
     * @return maximum number of players from the json file
     */
    public static int getMaxPlayers(){
        return getParameter("maxNumberOfPlayers");
    }

    /**
     * Method that gets the minimum number of players allowed in a game
     * @return minimum number of players from the json file
     */
    public static int getMinPlayers(){
        return getParameter("minNumberOfPlayers");
    }

    public static int getWinThreshold(){
        return getParameter("winThreshold");
    }

    public static int getPort() { return getParameter("serverPort"); }

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