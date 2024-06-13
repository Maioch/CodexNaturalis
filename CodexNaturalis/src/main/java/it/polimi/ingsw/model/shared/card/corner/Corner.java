package it.polimi.ingsw.model.shared.card.corner;


import it.polimi.ingsw.model.shared.Content;

import java.io.Serializable;

/**
 * Corner represents each of the four corners of a card.
 * Each corner has 3 coordinates: (x, y, visibility): x, y -> position in the player's board, visibility is false
 * if another corner is on top of it, true otherwise.
 */
public class Corner implements Serializable {

    private int x;
    private int y;
    private final Content content;
    private boolean visibility;
    private final Location location;

    /**
     * Class constructor.
     *
     * @param content the symbol the corner contains.
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
     * Returns the corner's horizontal coordinate on the board.
     *
     * @return the corner's X position.
     */
    public int getX(){
        return this.x;
    }

    /**
     * Returns the corner's vertical coordinate on the board.
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
     * Returns the position of the corner relative to the card.
     *
     * @return the corner's location.
     */
    public Location getLocation(){
        return this.location;
    }

    /**
     * Returns the symbol the corner contains.
     *
     * @return the corner's content.
     */
    public Content getContent(){
        return this.content;
    }

    /**
     * Returns false if the corner was covered by another one.
     *
     * @return the corner's visibility.
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
     * Checks if this corner and another are in the same position, more precisely if their coordinates coincide.
     *
     * @param otherCorner the corner to check with.
     *
     * @return true if this corner and the parameter one are in the same position.
     */
    public boolean isSamePosition(Corner otherCorner){
        return otherCorner.getX() == this.getX() && otherCorner.getY() == this.getY();
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param object corner checked.
     *
     * @return true if this corner is equal to the parameter one.
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