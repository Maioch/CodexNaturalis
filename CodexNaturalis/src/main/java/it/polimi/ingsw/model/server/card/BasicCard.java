package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.exceptions.CardException;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.Player;

import java.io.Serializable;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BasicCard represents a side of the simplest card in the game, namely resource cards and starter cards.
 * Each side has its related card's unique ID, a color and sometimes a points number; it also stores its
 * associated corners.
 * The resources attribute refers only to the resources found in the center of the side, not in the corners: it's
 * treated as a list because some sided have more than one symbol.
 * When the related card is drawn during a game, the player that owns it is also saved in this class.
 */
public class BasicCard implements Serializable {

    protected final int cardId;
    protected final Content color;
    protected final Set<Corner> corners;
    protected final int points;
    protected final List<Content> resources;
    protected final boolean isFront;
    protected transient Player owner;

    /**
     * Class constructor.
     *
     * @param cardId            the related card's id.
     * @param color             the side's color.
     * @param corners           the side's corners.
     * @param points            the side's points, that will be added to the player's score when he places the related card on
     *                          his board.
     * @param resources         the card's central resources.
     * @param isFront           the attribute used to check if the card is
     *
     * @exception CardException if the given parameters are invalid.
     */
    public BasicCard(
            int cardId,
            Content color,
            Set<Corner> corners,
            int points,
            List<Content> resources,
            boolean isFront) throws CardException {
        if(!color.isColor() || points < 0){
            throw new CardException(
                    String.format("Invalid card parameters on card with the following id:%d",cardId));
        }
        boolean allLocationsPresent = true;
        for(Location loc : Location.values()){
            if(corners.stream().noneMatch(c -> c.getLocation() == loc)){
                allLocationsPresent = false;
                break;
            }
        }
        if(!allLocationsPresent || corners.size() != Location.values().length){
            throw new CardException(
                    String.format("Malformed corner set encountered on card with the following id:%d",cardId));
        }
        this.cardId = cardId;
        this.color = color;
        this.corners = new HashSet<>(corners);
        this.points = points;
        this.resources = new ArrayList<>(resources);
        this.isFront = isFront;
    }

    /**
     * Class copy-constructor.
     *
     * @param card the instance to copy.
     */
    public BasicCard(BasicCard card){
        this.cardId = card.cardId;
        this.color = card.color;
        this.corners = new HashSet<>(){{
            for(Corner corner : card.corners){
                add(new Corner(corner));
            }
        }};
        this.points = card.points;
        this.resources = new ArrayList<>(card.resources);
        this.owner = card.owner;
        this.isFront = card.isFront;
    }

    /**
     * Returns the id of the card related to this side.
     *
     * @return the related card's id.
     */
    public int getCardId(){
        return this.cardId;
    }

    /**
     * Returns the points the card related to this side awards when it's placed by a player.
     *
     * @return the side's points.
     */
    public int getPoints(){
        return this.points;
    }

    /**
     * Returns the color of the card related to this side.
     *
     * @return the related card's color.
     */
    public Content getColor(){
        return this.color;
    }

    /**
     * Returns this side's corner at the specified location.
     *
     * @param loc the corner's location.
     *
     * @return    the requested corner.
     *
     * @see Location
     */
    public Corner getCorner(Location loc){
        return getAllCorners().stream().filter(c -> c.getLocation() == loc).findFirst().orElseThrow();
    }

    /**
     * Returns all the corners of this side.
     *
     * @return the side's corners.
     */
    public Set<Corner> getAllCorners(){
        Set<Corner> result = new HashSet<>();
        for(Corner corner : corners){
            result.add(new Corner(corner));
        }
        return result;
    }

    /**
     * Returns all the resources of this side's center.
     *
     * @return the side's central resources.
     */
    public List<Content> getResources(){
        return new ArrayList<>(this.resources);
    }

    /**
     * Returns a hashmap that associates each content type with the amount of occurrences in the card, by counting
     * from both the ones in the corners and the central resources.
     * This includes white and empty corners too.
     *
     * @return all the side's resources.
     */
    public Map<Content,Integer> getCardSymbols(){
        //Create a list containing all the contents of the card
        List<Content> totalContent = this.corners.stream()
                .filter(Corner::getVisibility)
                .map(Corner::getContent)
                .collect(Collectors.toCollection(ArrayList::new));
        totalContent.addAll(this.resources);
        return getMapFromContentList(totalContent);
    }

    /**
     * Converts a list of contents to a map containing each content as key and the times it appears in the list as value.
     *
     * @param totalContent the list to convert.
     *
     * @return             the converted hashmap.
     */
    protected Map<Content, Integer> getMapFromContentList(List<Content> totalContent){
        Map<Content, Integer> result = new HashMap<>();
        for(Content content : Content.values()){
            result.put(content, totalContent.stream()
                    .filter(x -> x == content)
                    .mapToInt(x -> 1)
                    .reduce(0, Integer::sum));
        }
        return result;
    }

    /**
     * Hides one of this side's corners when another one is placed on top of it.
     * Checks if the corner is actually in this side's corner list before the update.
     *
     * @param which the corner to cover.
     */
    public void coverCornerIfPresent(Corner which){
        for(Corner corner : corners){
            if(corner.equals(which)){
                corner.coverCorner();
            }
        }
    }

    /**
     * Returns the resources requested by the card to be placed.
     * These symbols must appear an adequate number of times on the player's board.
     *
     * @return the requirements needed to place the card.
     */
    public Map<Content, Integer> getRequirements(){
        return getMapFromContentList(new ArrayList<>());
    }

    /**
     * Places this side on the player's board.
     * It updates the coordinates of the corners.
     *
     * @param where the corner where the card will be placed.
     */
    public void place(Corner where){
        for(Corner corner : corners){
            corner.setX(where.getX() + corner.getLocation().getX() - where.getLocation().getOppositeLocation().getX());
            corner.setY(where.getY() + corner.getLocation().getY() - where.getLocation().getOppositeLocation().getY());
        }
    }

    /**
     * Checks if the side is the front or the back side of its related card.
     *
     * @return true if this is the front side.
     */
    public boolean isFront(){
        return isFront;
    }

    /**
     * Sets the owner of the card related to this side, which represents the player owning it.
     *
     * @param owner the related card's owner.
     */
    public void setOwner(Player owner){
        this.owner = owner;
    }

    /**
     * Returns a copy of this side, using the correct constructor.
     *
     * @return a copy of this side.
     */
    public BasicCard copy(){
        return new BasicCard(this);
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object side checked.
     *
     * @return       true if this side is equal to the parameter one.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof BasicCard other) {
            return this.cardId == other.cardId && this.isFront == other.isFront;
        }
        return false;
    }
}