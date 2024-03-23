package it.polimi.ingsw.model;

/**
 * Class that represents a corner of a card.
 * Every corner hs 3 coordinates: (x, y, visibility): x, y -> position on the player's card setup,
 * visibility -> false if another corner is on top of it or if it's a blank corner
 *
 * @author Marco Maiocchi
 */
public class Corner {
    private final int x, y;
    private final Content content;
    private boolean visibility;

    /**
     * Constructor for usable corners
     * @param x horizontal coordinate
     * @param y vertical coordinate
     * @param content symbol in the corner
     */
    public Corner(int x, int y, Content content){
        this.x = x;
        this.y = y;
        this.content = content;
        visibility = true;
    }

    /**
     * Constructor for blank corners (not superimposable corners)
     * @param x horizontal coordinate
     * @param y vertical coordinate
     */
    public Corner(int x, int y){
        this.x = x;
        this.y = y;
        this.content = null;
        visibility = false;
    }

    /**
     * Getter method for the x coordinate
     * @return x
     */
    public int getX(){
        return x;
    }

    /**
     * Getter method for the y coordinate
     * @return y
     */
    public int getY(){
        return y;
    }

    /**
     * Getter method for the content of the corner
     * @return content
     */
    public Content getContent(){
        return content;
    }

    /**
     * Getter method for the visibility of the corner
     * @return visibility
     */
    public boolean getVisibility(){
        return visibility;
    }

    /**
     * Sets corner visibility to false
     */
    public void coverCorner(){
        this.visibility = false;
    }

}
