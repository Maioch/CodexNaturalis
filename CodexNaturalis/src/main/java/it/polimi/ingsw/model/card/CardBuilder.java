package it.polimi.ingsw.model.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.awt.*;

/**
 * A class that creates all the cards (starter, resource, gold, objective) that are present in the game, by
 * reading their characteristics from a json file
 *
 * @author Guglielmo Gatti, Marco Maiocchi, Andrea Fidanza, Francesco Saverio Nisoli
 */
public class CardBuilder {
    private static final String filePath = "/resources/CardExample.json";

    /**
     * Method that creates resource/gold/starter cards
     * @param cardId id of the resource/gold/starter card to create
     * @return card created by the builder
     */
    public static CardSides buildCard(int cardId) {
        BasicCard cardFront;
        BasicCard cardBack;
        JsonNode cardJson = getCardJson(cardId, "placeableCards");
        Content color = Content.valueOf(cardJson.get("color").asText());
        HashMap<Location, Corner> frontCornerMap = getCorners(cardJson, "cornerFront");
        HashMap<Location, Corner> backCornerMap = getCorners(cardJson, "cornerBack");
        int points = cardJson.get("points").asInt();

        switch (cardJson.get("type").asText()) {

            case "RESOURCE":
                cardFront = new BasicCard(cardId, color, frontCornerMap, points, new ArrayList<>());
                cardBack = new BasicCard(cardId, color, backCornerMap, 0, new ArrayList<>() {{
                    add(color);
                }});

                break;

            case "GOLD":
                ArrayList<Content> requirements = getContentFromArray(cardJson, "requirements");
                GoldCard goldFront = new GoldCard(
                    new BasicCard(cardId, color, frontCornerMap, points, new ArrayList<>()), requirements);
                Bonus bonus = switch (cardJson.get("bonus").get("type").asText()) {
                    case "CORNER" -> goldFront.new CornerBonus();
                    case "OBJECT" -> {
                        Content object = Content.valueOf(cardJson.get("bonus").get("object").asText());
                        yield goldFront.new ObjectBonus(object);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + cardJson.get("bonus").get("type").asText());
                };
                goldFront.setBonus(bonus);
                cardFront = goldFront;
                cardBack = new BasicCard(cardId, color, backCornerMap, 0, new ArrayList<>() {{
                    add(color);
                }});

                break;

            case "STARTER":
                ArrayList<Content> resources = getContentFromArray(cardJson, "resources");
                cardFront = new BasicCard(cardId, Content.WHITE, frontCornerMap, 0, resources);
                cardBack = new BasicCard(cardId, Content.WHITE, backCornerMap, 0, new ArrayList<>());

                break;

            default: throw new IllegalStateException("Unexpected value: " + cardJson.get("type").asText());
        }

        return new CardSides(cardFront, cardBack);
    }

    /**
     * Method that creates objective cards
     * @param cardId id of the objective card to create
     * @return objective card created by the builder
     */
    public static Objective buildObjective(int cardId){
        JsonNode cardJson = getCardJson(cardId, "objectiveCards");

        return null;
    }

    /**
     * It creates the json node that represents the card corresponding to the given cardId by reading the json file
     *
     * @param cardId the card id
     * @param cardType the card type
     * @return the json node
     */
    private static JsonNode getCardJson(int cardId, String cardType){
        File cardsJson = new File(filePath);
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(cardsJson).get(cardType);
        }
        catch(IOException e){
            throw new RuntimeException(e.getMessage());
        }
        for(JsonNode subNode : node)
            if(subNode.get("id").asInt() == cardId)
                return subNode;
        throw new RuntimeException(String.format("The supplied card id couldn't be found %d",cardId));
    }

    /**
     * Method that returns a HashMap containing every location with the corresponding corner read from the json node
     *
     * @param cardJson json node that represents the card
     * @param fieldName field that we want to read
     * @return the corners HashMap
     */
    private static HashMap<Location, Corner> getCorners(JsonNode cardJson, String fieldName){
        JsonNode cornersJson = cardJson.get(fieldName);
        return new HashMap<>() {{
            for (Location loc : Location.values()) {
                put(loc, new Corner((!cardJson.has(fieldName)) ? Content.WHITE : Content.valueOf(cornersJson.get(loc.name()).asText())));
            }
        }};
    }

    /**
     *
     * @param cardJson
     * @param arrayName
     * @return
     */
    private static ArrayList<Content> getContentFromArray(JsonNode cardJson, String arrayName){
        return new ArrayList<>() {{
            for (JsonNode subNode : cardJson.get(arrayName)) {
                add(Content.valueOf(subNode.asText()));
            }
        }};
    }
}