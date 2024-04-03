package it.polimi.ingsw.model.card;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class CardBuilder {
    private static final String filePath = "../../../../../../../../resources/CardExample.json";
    public static CardSides buildCard(int cardId){
        BasicCard cardFront;
        BasicCard cardBack;
        JsonNode cardJson = getCardJson(cardId, "placeableCards");
        switch (cardJson.get("type").asText()){
            case "RESOURCE":
                Content color = Content.valueOf(cardJson.get("color").asText());
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode cornersJson = cardJson.get("corners");
                HashMap<Location,Corner> frontCornerMap = new HashMap<Location,Corner>(){{
                    for(Location loc : Location.values()){
                        put(loc, new Corner(Content.valueOf(cornersJson.get(loc.name()).asText())));
                    }
                }};
                int points = cardJson.get("points").asInt();
                cardFront = new BasicCard(cardId,color,frontCornerMap,points,new ArrayList<Content>());
                HashMap<Location,Corner> backCornerMap = new HashMap<Location,Corner>(){{
                    for(Location loc : Location.values()){
                        put(loc, new Corner(Content.WHITE));
                    }
                }};
                cardBack = new BasicCard(cardId,color,backCornerMap,0,new ArrayList<Content>(){{add(color);}});
                return new CardSides(cardFront,cardBack);
            case "GOLD":
                break;
            case "STARTER":
                break;
        }

        return null;
    }
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