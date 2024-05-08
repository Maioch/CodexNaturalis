package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;

import java.util.HashMap;
import java.util.Map;

public class CardFormatter {
    private final static int cardLength = 7;
    private final static Map<Content, String> contentRepresentation = new HashMap<>(){{
        for(Content c : Content.values()){
            put(c, c.getSymbol());
        }
    }};
    public static String getHandView(){
        return "";
    }
    public static String getCardView(BasicCard card){
        StringBuilder sb = new StringBuilder();
        sb.append(" ").append("_".repeat(cardLength - 2)).append(" \n");
        Map<Location, String> cornerString = new HashMap<>(){{
            for(Location loc : Location.values()){
                Corner currentCorner = card.getAllCorners().stream().filter(c -> c.getLocation() == loc).findFirst().orElseThrow();
                put(loc, contentRepresentation.get((currentCorner.getVisibility()) ? currentCorner.getContent() : Content.EMPTY));
            }
        }};
        sb.append(String.format("|%s  %s|\n", cornerString.get(Location.TL), cornerString.get(Location.TR)))
                .append("|    |\n")
                .append(String.format("|%s  %s|\n", cornerString.get(Location.BL), cornerString.get(Location.BR)));
        int offset = cardLength + 1 + cardLength / 2;
        for(Content c : card.getResources()){
            sb.insert(offset, contentRepresentation.get(c));
            offset += cardLength + 1;
        }
        sb.append(" ").append("‾".repeat(cardLength - 2)).append(" ");
        return sb.toString();
    }
}