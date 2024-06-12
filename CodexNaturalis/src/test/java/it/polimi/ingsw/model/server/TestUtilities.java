package it.polimi.ingsw.model.server;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;

import java.util.LinkedHashMap;
import java.util.Map;

public class TestUtilities {

    /**
     * Helper method used to automatically place a set of cards on the player's board
     * @param player the player who owns the board on which the method will place the cards on
     * @param relativePlacements a LinkedHashMap (to preserve insertion order) which pairs the id of each card
     *                           to the position of the corner it's going to be placed on
     * @param base a card that has already been placed to use as a starting point
     * @param useBack whether to retrieve the back sides or the front sides when placing the cards
     * @return the last card placed
     */
    public static BasicCard createTestBoard(Player player, LinkedHashMap<Integer, Location> relativePlacements, BasicCard base, boolean useBack){
        BasicCard previousCard = base;
        for(Map.Entry<Integer, Location> entry : relativePlacements.entrySet()){
            CardSides sides = CardBuilder.buildCard(entry.getKey());
            BasicCard card = useBack ? sides.backSide() : sides.frontSide();
            card.setOwner(player);
            Corner corner = previousCard.getAllCorners().stream()
                    .filter(c -> c.getLocation() == entry.getValue())
                    .findFirst().orElseThrow();
            player.placeCard(card,corner);
            previousCard = card;
        }
        return previousCard;
    }
}