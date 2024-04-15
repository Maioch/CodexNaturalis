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
 * Class that represents a "basic" card, which includes resource cards and starter cards.
 * It has a unique ID and has a specific color, with a maximum of 4 corners.
 * The resources attribute refers only to the resources found in the center of the card, not in the corners.
 *
 * @author Francesco Saverio Nisoli, Guglielmo Gatti
 */
public class BasicCard {

    protected final int cardId;
    protected final Content color;
    protected final HashSet<Corner> corners;
    protected final int points;
    protected final ArrayList<Content> resources;

    /**
     * Constructor for the class
     *
     * @param cardId the card's id
     * @param color the color of the card.
     * @param corners the card's corners (max 4)
     * @param points the card's points, that will be added to the player's score when he places the card on his board
     * @param resources the card's central resources, that can go from 0 to 3
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
     * Getter for the id of the card
     *
     * @return the cardId attribute
     */
    public int getCardId(){
        return this.cardId;
    }

    /**
     * Getter for the points of the card
     *
     * @return the points attribute
     */
    public int getPoints(){
        return this.points;
    }

    /**
     * Getter for the color of the card
     *
     * @return the card's color
     */
    public Content getColor(){
        return this.color;
    }

    /**
     * Getter for the corners of the card
     *
     * @return the corners hashmap
     */
    public HashSet<Corner> getAllCorners(){
        return new HashSet<>(corners);
    }

    /**
     * Returns a hashmap that associates each resource type with the amount present in the card by pulling
     * from both the corners and the permanent resources
     * IMPORTANT: this includes white and empty corners too
     *
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
                .filter(x -> !x.getContent().isEmpty())
                .filter(Corner::getVisibility)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * method called when a card gets placed onto a corner, hiding it.
     */
    public void coverCorner(Corner which){
        for(Corner corner : corners){
            if(corner.equals(which)){
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
     * @param where represents the corner where the card will be placed
     */
    public void place(Corner where){
        int offsetX = where.getLocation() == Location.TR || where.getLocation() == Location.BR ? 0 : -1;
        int offsetY = where.getLocation() == Location.TR || where.getLocation() == Location.TL ? 0 : -1;
        /*
        for(int x = 0; x < 2; x++){
            for(int y = 0; y < 2; y++){
                Corner corner = corners.stream()
                        .filter(c -> c.getLocation() == location)
                        .findAny()
                        .orElseThrow(() -> new RuntimeException(
                                String.format(
                                        "Card with id %d is missing a corner for location %s",
                                        cardId,
                                        location.toString())));
                corner.setX(x);
                corner.setY(y);
                //TONNO DOPO
                //Quando finisci qui aggiorna anche BasicCardTest
                //VA BENE
            }
        }*/
        for(Corner corner : corners){
            Point offset = new Point(where.getX() - offsetX,where.getY() - offsetY);
            switch(corner.getLocation()){
                case BR:
                    offset.translate(1 - offsetX,-offsetY);
                    break;
                case TL:
                    offset.translate(-offsetX,1 - offsetY);
                    break;
                case TR:
                    offset.translate(1 - offsetX,1 - offsetY);
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