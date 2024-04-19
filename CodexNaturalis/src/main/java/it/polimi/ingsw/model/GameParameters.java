package it.polimi.ingsw.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
     * Enum that represents the types of the cards
     */
    public enum CardType{
        RESOURCE, GOLD, STARTER, OBJECTIVE;
    }

    /**
     * Gets the start and end indexes of the specified card type
     * @param type the card type
     * @return the id of the first card of the requested type
     */
    public static int getStartCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "cardStartIndex");
    }

    /**
     * Gets the end indexes of the specified card type
     * @param type the card type
     * @return the id of the last card of the requested type
     */
    public static int getEndCardIndex(CardType type){
        return getParameter(type.toString().toLowerCase() + "cardEndIndex");
    }

    /**
     * Gets the number of visible cards read on the json file
     * @return the number of visible cards
     */
    public static int getNumberOfVisibleCards(){
        return getParameter("numberOfVisibleCards");
    }

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