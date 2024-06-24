package it.polimi.ingsw.model.shared.card;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.logging.Logger;

/**
 * Creates the cards and the objectives in the game, by reading their information from a json file.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see CardSides
 * @see Objective
 */
public class CardBuilder {

    //used to log issues with the card creation process.
    private static final Logger logger = Logger.getLogger(Parameters.getLoggerName());

    //the file that describes the game's cards.
    private static final String fileName = "cards.json";

    //the path where the file describing the cards is found.
    private static final String filePath = "/gameFiles/";

    /**
     * Creates resource/gold/starter cards.
     *
     * @param cardId the card's ID.
     *
     * @return       the card created.
     *
     * @see CardSides
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
                    default -> {
                        logger.severe("Unexpected value: " + cardJson.get("bonus").get("type").asText() + "\n");
                        throw new IllegalStateException();
                    }
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
            default -> {
                logger.severe("Unexpected value: " + cardJson.get("type").asText() + "\n");
                throw new IllegalStateException();
            }
        }
        return new CardSides(cardFront, cardBack);
    }

    /**
     * Creates objective cards.
     *
     * @param cardId the card's ID.
     *
     * @return       the card created.
     *
     * @see Objective
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
            default -> {
                logger.severe("Unexpected value: " + cardJson.get("bonus").get("type").asText() + "\n");
                throw new IllegalStateException();
            }
        };
        objective.setBonus(bonus);
        return objective;
    }

    /**
     * Gets a cards information by reading it from the json file.
     *
     * @param cardId the card id.
     *
     * @return       the json node containing the given card information.
     */
    public static JsonNode getCardJson(int cardId){
        String cardType = cardId < Parameters.getStartCardIndex(CardType.OBJECTIVE) ? "placeableCards" : "objectiveCards";
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(CardBuilder.class.getResource(filePath + fileName)).get(cardType);
        }
        catch(IOException e){
            logger.severe("Couldn't read the json file: " + e.getMessage() + "\n");
            throw new RuntimeException();
        }
        for(JsonNode subNode : node)
            if(subNode.get("id").asInt() == cardId)
                return subNode;
        logger.severe("The supplied card id couldn't be found " + cardId + "\n");
        throw new RuntimeException();
    }

    /**
     * Gets the color of a card.
     * If it's a starter card, returns white.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the card's color.
     *
     * @see Content
     */
    static Content getColor(JsonNode cardJson){
        return cardJson.has("color") ? Content.valueOf(cardJson.get("color").asText()) : Content.WHITE;
    }

    /**
     * Gets the points the card awards.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the card's points.
     */
    public static int getPoints(JsonNode cardJson){
        return cardJson.get("points").asInt();
    }

    /**
     * Gets a HashMap containing every location with the corresponding corner read from the json file.
     *
     * @param cardJson  json node that represents the card.
     * @param fieldName field to read.
     *
     * @return          the corners HashMap.
     *
     * @see Corner
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
     * @return          a list containing the same elements as the JSON array, converted from string to Content.
     *
     * @see Content
     */
    static List<Content> getContentFromArray(JsonNode cardJson, String arrayName){
        return new ArrayList<>() {{
            for (JsonNode subNode : cardJson.get(arrayName)) {
                add(Content.valueOf(subNode.asText()));
            }
        }};
    }

    /**
     * Gets the bonus type.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the String representing the bonus.
     */
    static String getBonusType(JsonNode cardJson){
        return cardJson.get("bonus").get("type").asText();
    }

    /**
     * Gets the object required for the bonus.
     *
     * @param cardJson json node that represents the card.
     *
     * @return         the object.
     *
     * @see Content
     */
    static Content getBonusContent(JsonNode cardJson){
        return Content.valueOf(cardJson.get("bonus").get("object").asText());
    }
}