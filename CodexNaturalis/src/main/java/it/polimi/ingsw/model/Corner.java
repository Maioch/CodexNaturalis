package it.polimi.ingsw.model;


/**
 * Class that represents a corner of a card.
 * Every corner hs 3 coordinates: (x, y, visibility): x, y -> position on the player's card setup,
 * visibility -> false if another corner is on top of it
 *
 * @author Marco Maiocchi
 */
public class Corner {
    private int x;
    private int y;
    private final Content content;
    private boolean visibility;

    /**
     * Constructor for corners
     * @param content symbol in the corner
     */
    public Corner(Content content){
        this.content = content;
        this.visibility = true;
        this.x = 0;
        this.y = 0;
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
     * setter for the X coordinate of the Corner
     * @param x the X coordinate where the corner is placed
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * setter for the Y coordinate of the Corner
     * @param y the Y coordinate where the corner is placed
     */
    public void setY(int y) {
        this.y = y;
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
}