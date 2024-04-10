package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * A class that represents a "basic" card, which includes resource cards and starter cards.
 * It has a unique ID, and has a specific color. There can be a maximum of 4 corners.
 * The resources attribute refers only to the resources found in the center of the card, not in the corners.
 * @author Francesco Saverio Nisoli, Guglielmo Gatti
 */
public class BasicCard {

    protected final int cardId;
    protected final Content color;
    protected final HashSet<Corner> corners;
    protected final int points;
    protected final ArrayList<Content> resources;

    /**
     * Constructor of the BasicCard
     * @param cardId the card's id
     * @param color the "color" of the card.
     * @param corners the card's corners, represented by
     * @param points the card's points, value which represent the sore's incrementation when the card is played
     * @param resources the card's resources, an array of the contained resources
     */

    public BasicCard(int cardId, Content color, HashSet<Corner> corners, int points, ArrayList<Content> resources) throws RuntimeException{

        if(!color.isColor() || points < 0){
            throw new RuntimeException(
                    String.format("Invalid card parameters on card with the following id:%d",cardId));
        }
        boolean allLocationsPresent = true;
        for(Location loc : Location.values()){
            if(corners.stream().noneMatch(c -> c.getLocation() == loc)){
                allLocationsPresent = false;
                break;
            }
        }
        if(corners.size() != 4 || !allLocationsPresent){
            throw new RuntimeException(
                    String.format("Malformed corner set encountered on card with the following id:%d",cardId));
        }

        this.cardId = cardId;
        this.color = color;
        this.corners = new HashSet<>(corners);
        this.points = points;
        this.resources = new ArrayList<>(resources);
    }

    /**
    * Getter of the "cardId" attribute
    * @return the cardId attribute
    */
    public int getCardId(){
        return this.cardId;
    }

    /**
     * Getter of the "points" attribute
     * @return the point attribute
     */
    public int getPoints(){
        return this.points;
    }

    /**
     * Getter of the "color" attribute
     * @return the card's color
     */
    public Content getColor(){
        return this.color;
    }

    /**
     * Returns a hashmap that associates each resource type with the amount present in the card by pulling
     * from both the corners and the permanent resources
     * IMPORTANT: this includes white and empty corners too
     * @return a hashmap with the resource as key and the amount as value
     */
    public HashMap<Content,Integer> getCardSymbols(){
        //Create a list containing all the contents of the card
        ArrayList<Content> totalContent = this.corners.stream()
                .filter(Corner::getVisibility)
                .map(Corner::getContent)
                .collect(Collectors.toCollection(ArrayList::new));
        totalContent.addAll(this.resources);
        return new HashMap<>(){{
            for(Content content : Content.values()){
                put(content, totalContent.stream()
                        .filter(x -> x == content)
                        .mapToInt(x -> 1)
                        .reduce(0,Integer::sum));
            }
        }};
    }

    /*consider changing the return values of getValidCorners and coverCorner to Location*/

    /**
     * Method used for retrieving all available corners for placing a card
     * @return the list of visible corners that aren't empty
     */
    public ArrayList<Corner> getValidCorners(){
        return this.corners.stream()
                .filter(x -> x.getContent() != Content.EMPTY)
                .filter(Corner::getVisibility)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Getter for the location and corner hashmap
     * @return the "corners" hashmap
     */
    public HashSet<Corner> getAllCorners(){
        return new HashSet<>(corners);
    }

    /**
     * method called when a card gets placed onto a corner, hiding it.
     */
    public void coverCorner(Corner which){
        for(Corner corner : corners){
            if(which == corner){
                corner.coverCorner();
            }
        }
    }

    /**
     * Getter of the "requirements" parameter
     * @return the requirements needed to play the card
     */
    public HashMap<Content, Integer> getRequirements(){
        return new HashMap<>(){{
            for(Content content : Content.values()){
                put(content, 0);
            }
        }};
    }

    /**
     * A public method to "place" the card. It initializes the coordinates of the corners (components of the card).
     * @param x represents the x-axis coordinate of the bottom-left corner where the card will be placed
     * @param y represents the y-axis coordinate of the bottom-left corner where the card will be placed
     */
    public void place(int x, int y){
        for(Corner corner : corners){
            Point offset = new Point(x, y);
            switch(corner.getLocation()){
                case Location.BR:
                    offset.translate(1,0);
                    break;
                case Location.TL:
                    offset.translate(0,1);
                    break;
                case Location.TR:
                    offset.translate(1,1);
                    break;
            }
            corner.setX(offset.x);
            corner.setY(offset.y);
        }
    }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object){
        if(this.getClass() != object.getClass())
            return false;
        BasicCard other = (BasicCard) object;
        other.resources.sort(Comparator.comparingInt(Enum::ordinal));
        return this.cardId == other.cardId &&
                this.color == other.color &&
                this.points == other.points &&
                this.resources.stream()
                    .sorted(Comparator.comparingInt(Enum::ordinal)).toList()
                    .equals(other.resources) &&
                this.corners.equals(other.corners);
    }
}