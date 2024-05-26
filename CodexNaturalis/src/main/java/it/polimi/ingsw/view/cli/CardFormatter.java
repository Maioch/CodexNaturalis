package it.polimi.ingsw.view.cli;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class that implements all the methods to format a card or the player's board, necessary operation to print
 * them in the CLI.
 */
public class CardFormatter {
    private final static int cardLength = 7;
    private final static int cardHeight = 5; //DO NOT CHANGE
    private final static int boardRadius = 4;

    /**
     * Method used to format a player's board.
     * @param placedCards the player's placed cards.
     * @param viewX the X coordinate to which the board is centered.
     * @param viewY the Y coordinate to which the board is centered.
     * @return the formatted board.
     */
    public static String getPlayerBoardString(List<BasicCard> placedCards, int viewX, int viewY){
        StringBuilder sb = new StringBuilder();
        int minX = viewX - boardRadius;
        int minY = viewY - boardRadius;
        int maxX = viewX + boardRadius;
        int maxY = viewY + boardRadius;
        int coordinateNumberWidth = 3;
        for(int y = maxY; y >= minY; y--){
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
                if(i + 1 == cardHeight * 4 / 5){
                    sb.append(String.format("%" + coordinateNumberWidth + "d", y));
                }
                else{
                    sb.append(" ".repeat(coordinateNumberWidth));
                }
                for(int x = minX; x <= maxX; x++){
                    int currentX = x;
                    Optional<BasicCard> currentCard = currentCards.stream()
                            .filter(b -> b.getCorner(Location.BL).getX() == currentX)
                            .findFirst();
                    String cardToAppend = " ".repeat(cardLength * 2 - 2);
                    if(currentCard.isPresent()){
                        cardToAppend = cardStrings.get(currentCards.indexOf(currentCard.get()))[i];
                    }
                    sb.append(cardToAppend);
                }
                sb.append("\n");
            }
        }
        sb.append(" ".repeat(coordinateNumberWidth));
        for(int i = minX; i <= maxX; i++){
            sb.append(String.format("%" + coordinateNumberWidth + "d", i))
                    .append(" ".repeat(cardLength * 2 - 2 - coordinateNumberWidth));
        }
        sb.append("\n");
        return sb.toString();
    }

    /**
     * Method that formats the message sent to explain an objective.
     * @param objective the objective to explain.
     * @return the message that represents the objective.
     */
    public static String getObjectiveInfoString(Objective objective){
        return String.format("gain %d points every time %s\n",
                objective.getPoints(),
                getBonusInfo(objective.getObjectiveId()));
    }

    /**
     * Method that formats the card info printed under its visual representation.
     * @param cards all the cards that need to be printed.
     * @return the formatted card's description.
     */
    public static String getCardsInfoString(List<BasicCard> cards){
        StringBuilder sb = new StringBuilder();
        int formatSpaceLength = 45;
        List<List<String>> cardStrings = new ArrayList<>(){{
            for(BasicCard card : cards){
                List<String> cardInfo = Arrays.stream(getCardString(card)
                        .split("\n"))
                        .collect(Collectors.toCollection(ArrayList::new));
                cardInfo.add(String.format("%" + (-formatSpaceLength) + "s", "Points: " +
                        (card.isFront() ? getNativePoints(card.getCardId()) : 0)));
                cardInfo.add(String.format("%" + (-formatSpaceLength) + "s", "Requirements: " +
                        (card.isFront() ? card.getRequirements().entrySet().stream().filter(e -> e.getValue() != 0).toList() : "[]" )));
                cardInfo.add(String.format("%" + (-formatSpaceLength) + "s", "Bonus type: " +
                        (card.isFront() ? getBonusInfo(card.getCardId()) : "no bonus.")));
                add(cardInfo);
            }
        }};
        for(int i = 0; !cardStrings.isEmpty() && i < cardStrings.getFirst().size(); i++){
            for(List<String> stringList : cardStrings){
                String string = stringList.get(i);
                sb.append(string);
                sb.append(" ".repeat(i < cardHeight ?
                         formatSpaceLength + 2 - cardLength * 2 :
                         Math.max(formatSpaceLength - string.length(), 0)));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Gets the textual representation of a card.
     * @param card the card to format.
     * @return the string representing the formatted card.
     */
    public static String getCardString(BasicCard card){
        if(card == null){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String empty = Content.EMPTY.getSymbol();
        sb.append(card.getColor().getTextColorString())
                .append(" ")
                .append("__".repeat(cardLength - 2))
                .append(" ")
                .append(Content.EMPTY.getTextColorString())
                .append("\n");
        Map<Location, String> cornerString = new HashMap<>(){{
            for(Location loc : Location.values()){
                Corner currentCorner = card.getAllCorners().stream().filter(c -> c.getLocation() == loc).findFirst().orElseThrow();
                put(loc, currentCorner.getVisibility() ? currentCorner.getContent().getSymbol() : Content.EMPTY.getSymbol());
            }
        }};
        List<String> resources = new ArrayList<>(){{
            List<Content> cardResources = card.getResources();
            for(int i = 0; i < cardHeight - 2; i++){
                add(i < cardResources.size() ? cardResources.get(i).getSymbol() : empty);
            }
        }};
        int emptyLengthWithCorners = (cardLength - 5) / 2;
        int emptyLengthWithoutCorners = (cardLength - 3) / 2;
        sb.append(String.format("%s|%s%s%s%s%s%s|%s\n",
                card.getColor().getTextColorString(),
                cornerString.get(Location.TL),
                empty.repeat(emptyLengthWithCorners),
                resources.getFirst(),
                empty.repeat(emptyLengthWithCorners),
                cornerString.get(Location.TR),
                card.getColor().getTextColorString(),
                Content.EMPTY.getTextColorString()));
        for(int i = 0; i < cardHeight - 4; i++) {
            sb.append(String.format("%s|%s%s%s%s|%s\n",
                    card.getColor().getTextColorString(),
                    empty.repeat(emptyLengthWithoutCorners),
                    resources.get(i + 1),
                    empty.repeat(emptyLengthWithoutCorners),
                    card.getColor().getTextColorString(),
                    Content.EMPTY.getTextColorString()));
        }
        sb.append(String.format("%s|%s%s%s%s%s%s|%s\n",
                card.getColor().getTextColorString(),
                cornerString.get(Location.BL),
                empty.repeat(emptyLengthWithCorners),
                resources.getLast(),
                empty.repeat(emptyLengthWithCorners),
                cornerString.get(Location.BR),
                card.getColor().getTextColorString(),
                Content.EMPTY.getTextColorString()));
        sb.append(card.getColor().getTextColorString())
                .append(" ")
                .append("‾‾".repeat(cardLength - 2))
                .append(" ")
                .append(Content.EMPTY.getTextColorString())
                .append("\n");
        return sb.toString();
    }

    /**
     * A method that gets a certain card bonus info.
     * @param cardId the id of the card we want to get the info from.
     * @return a formatted string of the bonus info.
     */
    private static String getBonusInfo(int cardId){
        JsonNode bonusNode = CardBuilder.getCardJson(cardId).get("bonus");
        if(bonusNode == null)
            return "no bonus.";
        return switch (bonusNode.get("type").asText()){
            case "CORNER" -> "corner.";
            case "OBJECT" -> "object: " + Content.valueOf(bonusNode.get("object").asText()).getSymbol();
            case "CONTENT" -> {
                List<String> contents = new ArrayList<>() {{
                    for (JsonNode subNode : bonusNode.get("content")) {
                        add(Content.valueOf(subNode.asText()).getSymbol());
                    }
                }};
                yield "*all* of the following content types appear: " + contents;
            }
            case "PATTERN" -> {
                StringBuilder sb = new StringBuilder();
                sb.append("the following pattern appears: \n\n");
                int minX = bonusNode.get("pattern").get(0).get("x").asInt();
                int maxX = bonusNode.get("pattern").get(0).get("x").asInt();
                int minY = bonusNode.get("pattern").get(0).get("y").asInt();
                int maxY = bonusNode.get("pattern").get(0).get("y").asInt();
                for(JsonNode subNode : bonusNode.get("pattern")) {
                    minX = Math.min(subNode.get("x").asInt(), minX);
                    maxX = Math.max(subNode.get("x").asInt(), maxX);
                    minY = Math.min(subNode.get("y").asInt(), minY);
                    maxY = Math.max(subNode.get("y").asInt(), maxY);
                }
                Map<Point, Content> patternMap = new HashMap<>();
                for(JsonNode subNode : bonusNode.get("pattern")) {
                    patternMap.put(new Point(subNode.get("x").asInt(),subNode.get("y").asInt()),
                            Content.valueOf(subNode.get("color").asText()));
                }
                Point prevPoint = new Point(minX,maxY);
                for(int y = maxY; y >= minY; y--){
                    for(int x = minX; x <= maxX; x++){
                        Point curPoint = new Point(x, y);
                        if(patternMap.containsKey(curPoint)){
                            Point relativePoint = new Point(curPoint.x - prevPoint.x, prevPoint.y - curPoint.y);
                            sb.append("\n".repeat(relativePoint.y));
                            sb.append("  ".repeat(curPoint.x - minX));
                            sb.append(patternMap.get(curPoint).getSymbol());
                            prevPoint = curPoint;
                        }
                    }
                }
                yield sb.toString();
            }
            default -> "no bonus.";
        };
    }

    /**
     * A method that gets a certain card native points.
     * @param cardId the id of the card we want to get the infos from.
     * @return the native points of the card.
     */
    private static int getNativePoints(int cardId){
        return CardBuilder.getPoints(CardBuilder.getCardJson(cardId));
    }
}