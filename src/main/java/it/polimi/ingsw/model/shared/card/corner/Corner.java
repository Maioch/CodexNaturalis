package it.polimi.ingsw.model.shared.card.corner;


import it.polimi.ingsw.model.shared.Content;

import java.io.Serializable;

/**
 * Represents each of the four corners of a card.
 * Each corner has 3 coordinates: (x, y, visibility): x, y -> position in the player's board, visibility is false
 * if another corner is on top of it, true otherwise.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Corner implements Serializable {

    //the corner's x coordinate.
    private int x;

    //the corner's y coordinate.
    private int y;

    //the corner's content (as in, the resource, object, or lack thereof)
    private final Content content;

    //whether a card has been placed on this corner
    private boolean visibility;

    //the corner's location, relative to the card it's part of.
    private final Location location;

    /**
     * Class constructor. It sets the coordinates to (0, 0, false).
     *
     * @param content  the symbol the corner contains.
     * @param location the corner's location.
     *
     * @see Content
     * @see Location
     */
    public Corner(Content content, Location location){
        this.content = content;
        this.location = location;
        this.visibility = true;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Class copy-constructor.
     *
     * @param corner the instance to be copied.
     */
    public Corner(Corner corner){
        this.content = corner.content;
        this.location = corner.location;
        this.visibility = corner.visibility;
        this.x = corner.x;
        this.y = corner.y;
    }

    /**
     * Gets the corner's horizontal coordinate on the board.
     *
     * @return the corner's X position.
     */
    public int getX(){
        return this.x;
    }

    /**
     * Gets the corner's vertical coordinate on the board.
     *
     * @return the corner's Y position.
     */
    public int getY(){
        return this.y;
    }

    /**
     * Sets the horizontal coordinate of the corner, when the associated card is placed.
     *
     * @param x the X coordinate to set.
     */
    public void setX(int x){
        this.x = x;
    }

    /**
     * Sets the vertical coordinate of the corner, when the associated card is placed.
     *
     * @param y the Y coordinate to set.
     */
    public void setY(int y){
        this.y = y;
    }

    /**
     * Gets the position of the corner relative to the card.
     *
     * @return the corner's location.
     *
     * @see Location
     */
    public Location getLocation(){
        return this.location;
    }

    /**
     * Gets the symbol the corner contains.
     *
     * @return the corner's content.
     *
     * @see Content
     */
    public Content getContent(){
        return this.content;
    }

    /**
     * Gets the corner's visibility.
     *
     * @return false if the corner was covered by another one.
     */
    public boolean getVisibility(){
        return this.visibility;
    }


    /**
     * Updates the visibility of the corner to false when another one is placed on top of it.
     */
    public void coverCorner(){
        this.visibility = false;
    }

    /**
     * Checks if this corner and another are in the same position, more precisely if they have equals x and y.
     *
     * @param otherCorner the corner to check with.
     *
     * @return            true if this corner and the parameter one are in the same position.
     */
    public boolean isSamePosition(Corner otherCorner){
        return otherCorner.getX() == this.getX() && otherCorner.getY() == this.getY();
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object corner checked.
     *
     * @return       true if this corner is equal to the parameter one.
     */
    @Override
    public boolean equals(Object object){
        if(object instanceof Corner corner) {
            return corner.x == this.x && corner.y == this.y &&
                    corner.visibility == this.visibility &&
                    corner.content == this.content &&
                    corner.location == this.location;
        }
        return false;
    }

    /**
     * Override of the hashCode function that enables the correct functionality
     * of the equals method for Hash-based data structures.
     * This *DOES* respect the contract for hashcode as it is only required for the method to return
     * the same value for equal objects, while it is not needed for different
     * objects to have different hash codes.
     *
     * @return an arbitrary integer.
     */
    @Override
    public int hashCode(){
        return 1;
    }
}