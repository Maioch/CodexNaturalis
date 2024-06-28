package it.polimi.ingsw.view.cli;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardBuilder;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides static methods used in the CLI to format cards, player boards and objectives.
 * This class is needed to give a visual representation of the game's state to the player using the command line interface.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CardFormatter {

    //the number of characters printed for a card length-wise.
    private final static int cardLength = 7;

    //the number of characters printed for a card height-wise.
    private final static int cardHeight = 5;

    //the number of cards viewable on each side of the board, excluding the card in the center.
    private final static int boardRadius = 4;

    /**
     * Class constructor.
     */
    public CardFormatter(){}

    /**
     * Formats the player's board.
     *
     * @param placedCards the player's current placed cards.
     * @param viewX       the board's center X coordinate.
     * @param viewY       the board's center Y coordinate.
     *
     * @return            the formatted player's board.
     *
     * @see BasicCard
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
     * Formats the sentence that explains when an objective awards its points.
     *
     * @param objective the objective to explain.
     *
     * @return          the formatted objective's message.
     *
     * @see Objective
     */
    public static String getObjectiveInfoString(Objective objective){
        return String.format("gain %d points every time %s",
                objective.getPoints(),
                getBonusInfo(objective.getObjectiveId()));
    }

    /**
     * Formats the card information, such as the points it awards, the resources required to place it and the eventual
     * bonus type.
     * This information are printed under the card's visual representation whenever the player needs to see the cards
     * in his hand, for whatever reason.
     *
     * @param cards the cards whose information needs to be printed.
     *
     * @return      the formatted card's description.
     *
     * @see BasicCard
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
     * Formats the given card.
     * Returns the visual representation of the card with all it's related symbols.
     *
     * @param card the card to print.
     *
     * @return     the formatted card.
     *
     * @see BasicCard
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
     * Formats the given card's bonus information.
     * The strings returned are used to complement ones returned by other methods in this class.
     *
     * @param cardId the card whose bonus information needs to be printed.
     *
     * @return       the formatted bonus information.
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
                for(int y = maxY; y >= minY; y--){
                    sb.append("      ");
                    for(int x = minX; x <= maxX; x++){
                        Point curPoint = new Point(x, y);
                        sb.append((patternMap.containsKey(curPoint)) ? patternMap.get(curPoint).getSymbol() : "  ");
                    }
                    sb.append("\n");
                }
                yield sb.toString();
            }
            default -> "no bonus.";
        };
    }

    /**
     * Gets the points awarded by the given card.
     *
     * @param cardId the card whose points are needed.
     *
     * @return       the card's awarded points.
     */
    private static int getNativePoints(int cardId){
        return CardBuilder.getPoints(CardBuilder.getCardJson(cardId));
    }
}