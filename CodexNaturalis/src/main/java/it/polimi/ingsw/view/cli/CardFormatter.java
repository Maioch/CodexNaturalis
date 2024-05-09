package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CardFormatter {
    private final static int cardLength = 7;
    private final static int cardHeight = 5; //DO NOT CHANGE
    private final static Map<Content, String> contentRepresentation = new HashMap<>(){{
        for(Content c : Content.values()){
            put(c, c.getSymbol());
        }
    }};

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
                    UtilitiesCLI.getNativePoints(card, "placeableCards")));
        }
        sb.append("\n");
        for(BasicCard card : cards){
            sb.append(String.format("%" + formatSpaceLength + "s", "Requirements: " +
                    card.getRequirements().entrySet().stream().filter(e -> e.getValue() != 0).toList()));
        }
        sb.append("\n");
        for(BasicCard card : cards){
            sb.append(String.format("%" + formatSpaceLength + "s", "Bonus type: " +
                    UtilitiesCLI.getBonusInfo(card, "placeableCards")));
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
}