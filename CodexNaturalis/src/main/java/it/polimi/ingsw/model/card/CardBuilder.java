package it.polimi.ingsw.model.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.*;
import it.polimi.ingsw.model.card.corner.Corner;
import it.polimi.ingsw.model.card.corner.Location;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.*;

/**
 * A class that creates all the cards (starter, resource, gold, objective) that are present in the game, by
 * reading their characteristics from a json file
 *
 * @author Guglielmo Gatti, Marco Maiocchi, Andrea Fidanza, Francesco Saverio Nisoli
 */
public class CardBuilder {
    private static final String fileName = "cards.json";
    private static final String filePath = "resources/";

    /**
     * Method that creates resource/gold/starter cards
     * @param cardId id of the resource/gold/starter card to create
     * @return card created by the builder
     */
    public static CardSides buildCard(int cardId) {
        BasicCard cardFront;
        BasicCard cardBack;
        JsonNode cardJson = getCardJson(cardId, "placeableCards");
        Content color = getColor(cardJson);
        HashSet<Corner> frontCornerMap = getCorners(cardJson, "cornersFront");
        HashSet<Corner> backCornerMap = getCorners(cardJson, "cornersBack");
        int points = getPoints(cardJson);
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
                Bonus bonus = switch (getBonusType(cardJson)) {
                    case "CORNER" -> goldFront.new CornerBonus();
                    case "OBJECT" -> {
                        Content object = getBonusContent(cardJson);
                        yield goldFront.new ObjectBonus(object);
                    }
                    case "NOTHING" -> null;
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
        int id = cardJson.get("id").asInt();
        int points = cardJson.get("points").asInt();
        Objective objective = new Objective(id,points);
        Bonus bonus = switch (cardJson.get("bonus").get("type").asText()){
            case "CONTENT" -> {
                ArrayList<Content> contents = getContentFromArray(cardJson.get("bonus"), "content");
                yield objective.new ContentBonus(contents);
            }
            case "PATTERN" -> {
                HashMap<Point, Content> pattern = new HashMap<>(){{
                    for(JsonNode subNode : cardJson.get("bonus").get("pattern")){
                        int x = subNode.get("x").asInt();
                        int y = subNode.get("y").asInt();
                        Content color = Content.valueOf(subNode.get("color").asText());
                        put(new Point(x,y),color);
                    }
                }};
                yield objective.new PatternBonus(pattern);
            }
            default ->
                throw new IllegalStateException("Unexpected value: " + cardJson.get("bonus").get("type").asText());
        };
        objective.setBonus(bonus);
        return objective;
    }

    /**
     * It creates the json node that represents the card corresponding to the given cardId by reading the json file
     *
     * @param cardId the card id
     * @param cardType the card type
     * @return the json node
     */
    static JsonNode getCardJson(int cardId, String cardType){
        File cardsJson = new File(filePath + fileName);
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
     * Method that returns the color read from the json file
     *
     * @param cardJson json node that represents the card
     * @return the read color
     */
    static Content getColor(JsonNode cardJson){
        return cardJson.has("color") ? Content.valueOf(cardJson.get("color").asText()) : Content.WHITE;
    }


    static int getPoints(JsonNode cardJson){
        return cardJson.get("points").asInt();
    }

    /**
     * Method that returns a HashMap containing every location with the corresponding corner read from the json node
     * @param cardJson json node that represents the card
     * @param fieldName field that we want to read
     * @return the corners HashMap
     */
    static HashSet<Corner> getCorners(JsonNode cardJson, String fieldName){
        JsonNode cornersJson = cardJson.get(fieldName);
        return new HashSet<>() {{
            for (Location loc : Location.values()) {
                add(new Corner(!cardJson.has(fieldName) ? Content.WHITE : Content.valueOf(cornersJson.get(loc.name()).asText()), loc));
            }
        }};
    }

    /**
     * Helper method used to convert a JSON array to a Content ArrayList
     * @param cardJson the JsonNode that represents the root of a single card's data
     * @param arrayName the name of the array property
     * @return an ArrayList containing the same elements as the JSON array, converted from string to Content
     */
    static ArrayList<Content> getContentFromArray(JsonNode cardJson, String arrayName){
        return new ArrayList<>() {{
            for (JsonNode subNode : cardJson.get(arrayName)) {
                add(Content.valueOf(subNode.asText()));
            }
        }};
    }

    /**
     * Helper method used to get the bonus type
     * @param cardJson the JsonNode that represents the root of a single card's data
     * @return the String representing the bonus
     */
    static String getBonusType(JsonNode cardJson){
        return cardJson.get("bonus").get("type").asText();
    }

    /**
     * Helper method used to get the object required for the bonus
     * @param cardJson the JsonNode that represents the root of a single card's data
     * @return the object
     */
    static Content getBonusContent(JsonNode cardJson){
        return Content.valueOf(cardJson.get("bonus").get("object").asText());
    }
}