package it.polimi.ingsw.view.cli;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;

import java.util.*;
import java.util.List;

public class CardFormatter {
    private final static int cardLength = 7;
    private final static int cardHeight = 5; //DO NOT CHANGE
    private final static int boardRadius = 4;
    private final static Map<Content, String> contentRepresentation = new HashMap<>(){{
        for(Content c : Content.values()){
            put(c, c.getSymbol());
        }
    }};

    public static String getPlayerBoardString(List<BasicCard> placedCards, int viewX, int viewY){
        StringBuilder sb = new StringBuilder();
        int minX = viewX - boardRadius;
        int minY = viewY - boardRadius;
        int maxX = viewX + boardRadius;
        int maxY = viewY + boardRadius;
        int coordinateNumberWidth = 2;
        sb.append(" ".repeat(coordinateNumberWidth));
        for(int i = minX; i < maxX; i++){
            sb.append(String.format("%" + coordinateNumberWidth + "d", i))
                    .append("  ".repeat(cardLength - coordinateNumberWidth));
        }
        sb.append("\n");
        for(int y = minY; y <= maxY; y++){
            sb.append(String.format("%" + coordinateNumberWidth + "d", y));
            int currentY = y;
            List<BasicCard> currentCards = placedCards.stream()
                    .filter(b -> b.getCorner(Location.BL).getY() == currentY &&
                            b.getCorner(Location.BL).getX() >= minX &&
                            b.getCorner(Location.BL).getX() <= maxX)
                    .toList();
            List<String[]> cardStrings = new ArrayList<>();
            for (BasicCard card : currentCards){
                cardStrings.add(getCardString(card).split("\n"));
            }
            for(int i = 0; i < cardHeight; i++){
                for(int x = minX; x <= maxX; x++){
                    int currentX = x;
                    Optional<BasicCard> currentCard = currentCards.stream()
                            .filter(b -> b.getCorner(Location.BL).getX() == currentX)
                            .findFirst();
                    String cardToAppend = "  ".repeat(cardLength);
                    if(currentCard.isPresent()){
                        cardToAppend = cardStrings.get(currentCards.indexOf(currentCard.get()))[i];
                    }
                    sb.append(cardToAppend);
                }
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static String getObjectiveInfoString(Objective objective){
        return String.format("objective card that awards %d points every time %s",
                objective.getPoints(),
                getBonusInfo(objective.getObjectiveId()));
    }

    /**
     * Gets the textual representation of a card's list
     * @param cards list of the card's list
     * @return the string representing the formatted card
     */
    public static String getCardsInfoString(List<BasicCard> cards){
        StringBuilder sb = new StringBuilder();
        int formatSpaceLength = -45;
        for(int i = 0; i < cardHeight; i++) {
            for (BasicCard card : cards) {
                sb.append(String.format("%" + formatSpaceLength + "s", getCardString(card).split("\n")[i]));
            }
            sb.append("\n");
        }
        for(BasicCard card : cards){
            sb.append(String.format("%" + formatSpaceLength + "s", "Points: " +
                    getNativePoints(card.getCardId())));
        }
        sb.append("\n");
        for(BasicCard card : cards){
            sb.append(String.format("%" + formatSpaceLength + "s", "Requirements: " +
                    card.getRequirements().entrySet().stream().filter(e -> e.getValue() != 0).toList()));
        }
        sb.append("\n");
        for(BasicCard card : cards){
            sb.append(String.format("%" + formatSpaceLength + "s", "Bonus type: " +
                    getBonusInfo(card.getCardId())));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Gets the textual representation of a card
     * @param card the card to format
     * @return the string representing the formatted card
     */
    private static String getCardString(BasicCard card){
        StringBuilder sb = new StringBuilder();
        String empty = Content.EMPTY.getSymbol();
        sb.append(empty).append("__".repeat(cardLength - 2)).append(empty).append("\n");
        Map<Location, String> cornerString = new HashMap<>(){{
            for(Location loc : Location.values()){
                Corner currentCorner = card.getAllCorners().stream().filter(c -> c.getLocation() == loc).findFirst().orElseThrow();
                put(loc, contentRepresentation.get((currentCorner.getVisibility()) ? currentCorner.getContent() : Content.EMPTY));
            }
        }};
        sb.append(String.format(" |%s%s%s| \n", cornerString.get(Location.TL), empty.repeat(cardLength - 4 - 1), cornerString.get(Location.TR)))
                .append(String.format(" |%s| \n", empty.repeat(cardLength - 2 - 1)))
                .append(String.format(" |%s%s%s| \n", cornerString.get(Location.BL), empty.repeat(cardLength - 4 - 1), cornerString.get(Location.BR)));
        int offset = cardLength * 2 + cardLength;
        for(int i = 0; i < cardHeight - 2; i++){
            sb.insert(offset, (i < card.getResources().size()) ? contentRepresentation.get(card.getResources().get(i)) : empty);
            offset += cardLength * 2 + 1;
        }
        sb.append(empty).append("‾‾".repeat(cardLength - 2)).append(empty).append("\n");
        return sb.toString();
    }

    /**
     * A method that gets a certain card bonus infos
     * @param cardId the id of the card we want to get the infos from
     * @return a formatted string of the bonus infos
     */
    private static String getBonusInfo(int cardId){
        JsonNode bonusNode = CardBuilder.getCardJson(cardId).get("bonus");
        if(bonusNode == null)
            return "no bonus.";
        return switch (bonusNode.get("type").asText()){
            case "CORNER" -> "corner.";
            case "OBJECT" -> "object -> " + bonusNode.get("object").asText();
            case "CONTENT" -> "*all* of the following content types appear -> " + bonusNode.get("content").asText();
            case "PATTERN" -> {
                StringBuilder sb = new StringBuilder();
                sb.append("the following pattern appears ->");
                for(JsonNode subNode : bonusNode.get("pattern")){
                    sb.append("  ".repeat(subNode.get("x").asInt()));
                    sb.append("\n".repeat(subNode.get("y").asInt()));
                    sb.append(Content.valueOf(subNode.get("color").asText()).getSymbol());
                }
                yield sb.toString();
            }
            default -> "no bonus.";
        };
    }

    /**
     * A method that gets a certain card native points
     * @param cardId the id of the card we want to get the infos from
     * @return the native points
     */
    private static int getNativePoints(int cardId){
        return CardBuilder.getPoints(CardBuilder.getCardJson(cardId));
    }
}