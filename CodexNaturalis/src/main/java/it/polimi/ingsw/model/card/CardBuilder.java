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
                break;
        }

        return null;
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

        JsonNode foundCard = null;
        for(JsonNode subNode : node)
            if(subNode.get("id").asInt() == cardId)
                return subNode;

        throw new RuntimeException(String.format("The supplied card id couldn't be found %d",cardId));
    }
}