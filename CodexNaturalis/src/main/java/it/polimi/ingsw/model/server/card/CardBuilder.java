package it.polimi.ingsw.model.server.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.model.server.Content;

import java.io.IOException;
import java.util.*;
import java.awt.*;
import java.util.List;

/**
 * A class that creates all the cards (starter, resource, gold, objective) that are present in the game, by
 * reading their characteristics from a json file.
 *
 * @author Guglielmo Gatti, Marco Maiocchi, Andrea Fidanza, Francesco Saverio Nisoli
 */
public class CardBuilder {
    private static final String fileName = "cards.json";
    private static final String filePath = "/gameFiles/";

    /**
     * Method that creates resource/gold/starter cards.
     * @param cardId id of the resource/gold/starter card to create.
     * @return card created by the builder.
     */
    public static CardSides buildCard(int cardId) {
        BasicCard cardFront;
        BasicCard cardBack;
        JsonNode cardJson = getCardJson(cardId);
        Content color = getColor(cardJson);
        Set<Corner> frontCornerMap = getCorners(cardJson, "cornersFront");
        Set<Corner> backCornerMap = getCorners(cardJson, "cornersBack");
        int points = getPoints(cardJson);
        switch (cardJson.get("type").asText()) {
            case "RESOURCE" -> {
                cardFront = new BasicCard(cardId, color, frontCornerMap, points, new ArrayList<>(), true);
                cardBack = new BasicCard(cardId, color, backCornerMap, 0, new ArrayList<>() {{add(color);}}, false);
            }
            case "GOLD" -> {
                List<Content> requirements = getContentFromArray(cardJson, "requirements");
                GoldCard goldFront = new GoldCard(
                        new BasicCard(cardId, color, frontCornerMap, points, new ArrayList<>(), true), requirements);
                Bonus bonus = switch (getBonusType(cardJson)) {
                    case "CORNER" -> goldFront.new CornerBonus();
                    case "OBJECT" -> {
                        Content object = getBonusContent(cardJson);
                        yield goldFront.new ObjectBonus(object);
                    }
                    case "NOTHING" -> null;
                    default ->
                            throw new IllegalStateException("Unexpected value: " + cardJson.get("bonus").get("type").asText());
                };
                goldFront.setBonus(bonus);
                cardFront = goldFront;
                cardBack = new BasicCard(cardId, color, backCornerMap, 0, new ArrayList<>() {{ add(color); }}, false);
            }
            case "STARTER" -> {
                List<Content> resources = getContentFromArray(cardJson, "resources");
                cardFront = new BasicCard(cardId, Content.WHITE, frontCornerMap, 0, resources, true);
                cardBack = new BasicCard(cardId, Content.WHITE, backCornerMap, 0, new ArrayList<>(), false);
            }
            default -> throw new IllegalStateException("Unexpected value: " + cardJson.get("type").asText());
        }
        return new CardSides(cardFront, cardBack);
    }

    /**
     * Method that creates objective cards.
     * @param cardId id of the objective card to create.
     * @return objective card created by the builder.
     */
    public static Objective buildObjective(int cardId){
        JsonNode cardJson = getCardJson(cardId);
        int id = cardJson.get("id").asInt();
        int points = cardJson.get("points").asInt();
        Objective objective = new Objective(id,points);
        Bonus bonus = switch (cardJson.get("bonus").get("type").asText()){
            case "CONTENT" -> {
                List<Content> contents = getContentFromArray(cardJson.get("bonus"), "content");
                yield objective.new ContentBonus(contents);
            }
            case "PATTERN" -> {
                Map<Point, Content> pattern = new HashMap<>(){{
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
     * Method that creates the json node that represents the card corresponding to the given cardId by reading the json file.
     * @param cardId the card id.
     * @return the json node.
     */
    public static JsonNode getCardJson(int cardId){
        String cardType = cardId < GameParameters.getStartCardIndex(CardType.OBJECTIVE) ? "placeableCards" : "objectiveCards";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(CardBuilder.class.getResource(filePath + fileName)).get(cardType);
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
     * @param cardJson json node that represents the card.
     * @return the read color.
     */
    static Content getColor(JsonNode cardJson){
        return cardJson.has("color") ? Content.valueOf(cardJson.get("color").asText()) : Content.WHITE;
    }

    /**
     * @param cardJson json node that represents the card.
     * @return the read points.
     */
    public static int getPoints(JsonNode cardJson){
        return cardJson.get("points").asInt();
    }

    /**
     * Method that returns a HashMap containing every location with the corresponding corner read from the json node.
     * @param cardJson json node that represents the card.
     * @param fieldName field that we want to read.
     * @return the corners HashMap.
     */
    static Set<Corner> getCorners(JsonNode cardJson, String fieldName){
        JsonNode cornersJson = cardJson.get(fieldName);
        Set<Corner> result = new HashSet<>();
        for (Location loc : Location.values()) {
            result.add(new Corner(!cardJson.has(fieldName) ? Content.WHITE : Content.valueOf(cornersJson.get(loc.name()).asText()), loc));
        }
        return result;
    }

    /**
     * Helper method used to convert a JSON array to a Content ArrayList.
     * @param cardJson the JsonNode that represents the root of a single card's data.
     * @param arrayName the name of the array property.
     * @return an ArrayList containing the same elements as the JSON array, converted from string to Content.
     */
    static List<Content> getContentFromArray(JsonNode cardJson, String arrayName){
        return new ArrayList<>() {{
            for (JsonNode subNode : cardJson.get(arrayName)) {
                add(Content.valueOf(subNode.asText()));
            }
        }};
    }

    /**
     * Helper method used to get the bonus type.
     * @param cardJson the JsonNode that represents the root of a single card's data.
     * @return the String representing the bonus.
     */
    static String getBonusType(JsonNode cardJson){
        return cardJson.get("bonus").get("type").asText();
    }

    /**
     * Helper method used to get the object required for the bonus.
     * @param cardJson the JsonNode that represents the root of a single card's data.
     * @return the object.
     */
    static Content getBonusContent(JsonNode cardJson){
        return Content.valueOf(cardJson.get("bonus").get("object").asText());
    }
}