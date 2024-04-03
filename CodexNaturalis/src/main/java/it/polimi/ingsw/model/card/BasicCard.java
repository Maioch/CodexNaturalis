package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;

import java.util.ArrayList;
import java.util.HashMap;
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
    protected final HashMap<Location, Corner> corners;
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

    BasicCard(int cardId, Content color, HashMap<Location, Corner> corners, int points, ArrayList<Content> resources) throws RuntimeException{

        if(!color.isColor() || points < 0){
            throw new RuntimeException();
        }

        this.cardId = cardId;
        this.color = color;
        this.corners = new HashMap<>(corners);
        this.points = points;
        this.resources = new ArrayList<>(resources);
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
        ArrayList<Content> totalContent = this.corners.values().stream()
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
        return this.corners.values().stream()
                .filter(x -> x.getContent() != Content.EMPTY)
                .filter(Corner::getVisibility)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Getter for the location and corner hashmap
     * @return the "corners" hashmap
     */
    public HashMap<Location, Corner> getAllCorners(){
        return new HashMap<>(corners);
    }

    /**
     * method called when a card gets placed onto a corner, hiding it.
     */
    public void coverCorner(Corner which){
        for(Corner corner : corners.values()){
            if(which == corner){
                corner.coverCorner();
            }
        }
    }

    /**
     * Getter of the "requirements" parameter
     * @return the requirements needed to play the card
     */
    public ArrayList<Content> getRequirements(){
        return new ArrayList<Content>();
    }

    /**
     * A public method to "place" the card. It initializes the coordinates of the corners (components of the card).
     * @param x represents the x-axis coordinate of the bottom-left corner where the card will be placed
     * @param y represents the y-axis coordinate of the bottom-left corner where the card will be placed
     */
    public void place(int x, int y){

        corners.get(Location.BL).setX(x);
        corners.get(Location.BL).setY(y);

        corners.get(Location.BR).setX(x+1);
        corners.get(Location.BR).setY(y);

        corners.get(Location.TL).setX(x);
        corners.get(Location.TL).setY(y+1);

        corners.get(Location.TR).setX(x+1);
        corners.get(Location.TR).setY(y+1);

    }
}