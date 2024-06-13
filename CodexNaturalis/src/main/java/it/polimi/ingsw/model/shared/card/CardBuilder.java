package it.polimi.ingsw.model.shared.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;

/**
 * CardBuilder creates the cards in the game, by reading their information from a json file.
 */
public class CardBuilder {

    private static final String fileName = "cards.json";
    private static final String filePath = "/gameFiles/";

    /**
     * Creates resource/gold/starter cards.
     *
     * @param cardId the card's ID.
     *
     * @return       the card created.
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
     * Creates objective cards.
     *
     * @param cardId the card's ID.
     *
     * @return       the card created.
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
     * Returns a cards information by reading it from the json file.
     *
     * @param cardId the card id.
     *
     * @return       the json node containing the given card information.
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
     * Returns the color of a card.
     * If it's a starter card, returns white.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the card's color.
     */
    static Content getColor(JsonNode cardJson){
        return cardJson.has("color") ? Content.valueOf(cardJson.get("color").asText()) : Content.WHITE;
    }

    /**
     * Returns the points the card awards.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the card's points.
     */
    public static int getPoints(JsonNode cardJson){
        return cardJson.get("points").asInt();
    }

    /**
     * Returns a HashMap containing every location with the corresponding corner read from the json file.
     *
     * @param cardJson  json node that represents the card.
     * @param fieldName field to read.
     * @return          the corners HashMap.
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
     * Converts a JSON array to a Content ArrayList.
     *
     * @param cardJson  json node that represents the card.
     * @param arrayName the name of the array property.
     *
     * @return a list containing the same elements as the JSON array, converted from string to Content.
     */
    static List<Content> getContentFromArray(JsonNode cardJson, String arrayName){
        return new ArrayList<>() {{
            for (JsonNode subNode : cardJson.get(arrayName)) {
                add(Content.valueOf(subNode.asText()));
            }
        }};
    }

    /**
     * Returns the bonus type.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the String representing the bonus.
     */
    static String getBonusType(JsonNode cardJson){
        return cardJson.get("bonus").get("type").asText();
    }

    /**
     * Returns the object required for the bonus.
     *
     * @param cardJson json node that represents the card.
     * @return         the object.
     */
    static Content getBonusContent(JsonNode cardJson){
        return Content.valueOf(cardJson.get("bonus").get("object").asText());
    }
}