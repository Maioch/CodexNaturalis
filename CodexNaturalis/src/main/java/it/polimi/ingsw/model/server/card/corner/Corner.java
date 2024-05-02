package it.polimi.ingsw.model.server.card.corner;


import it.polimi.ingsw.model.server.Content;

import java.io.Serializable;

/**
 * Class that represents a corner of a card.
 * Every corner hs 3 coordinates: (x, y, visibility): x, y -> position on the player's card setup,
 * visibility -> false if another corner is on top of it
 *
 * @author Marco Maiocchi
 */
public class Corner implements Serializable {
    private int x;
    private int y;
    private final Content content;
    private boolean visibility;
    private final Location location;

    /**
     * Class constructor
     *
     * @param content symbol in the corner
     */
    public Corner(Content content, Location location){
        this.content = content;
        this.location = location;
        this.visibility = true;
        this.x = 0;
        this.y = 0;
    }

    /**
     * Copy constructor for corner, used to avoid direct object access to other methods
     * @param corner the corner to be copied
     */
    public Corner(Corner corner){
        this.content = corner.content;
        this.location = corner.location;
        this.visibility = corner.visibility;
        this.x = corner.x;
        this.y = corner.y;
    }

    /**
     * @return corner's horizontal coordinate
     */
    public int getX(){
        return this.x;
    }

    /**
     * @return corner's vertical coordinate
     */
    public int getY(){
        return this.y;
    }

    /**
     * @param x the X coordinate where the corner is placed
     */
    public void setX(int x){
        this.x = x;
    }

    /**
     * @param y the Y coordinate where the corner is placed
     */
    public void setY(int y){
        this.y = y;
    }

    /**
     * @return corner's location
     */
    public Location getLocation(){
        return this.location;
    }

    /**
     * @return corner's content
     */
    public Content getContent(){
        return this.content;
    }

    /**
     * @return corner's visibility
     */
    public boolean getVisibility(){
        return this.visibility;
    }


    /**
     * Sets corner's visibility to false
     */
    public void coverCorner(){
        this.visibility = false;
    }

    /**
     * @param otherCorner another corner
     * @return true if this corner and thew param one are in the same position (they have the same x and y coords)
     */
    public boolean isSamePosition(Corner otherCorner){
        return otherCorner.getX() == this.getX() && otherCorner.getY() == this.getY();
    }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object){
        if(object.getClass() != this.getClass()){
            return false;
        }
        Corner corner = (Corner) object;
        return corner.x == this.x && corner.y == this.y &&
                corner.visibility == this.visibility &&
                corner.content == this.content &&
                corner.location == this.location;
    }

    /**
     * Override of the hashCode function that enables the correct functionality
     * of the equals method for Hash-based data structures. This *DOES* respect
     * the contract for hashcode as it is only required for the method to return
     * the same value for equal objects, while it is not needed for different
     * objects to have different hash codes.
     * @return an arbitrary integer
     */
    @Override
    public int hashCode(){
        return 1;
    }
}