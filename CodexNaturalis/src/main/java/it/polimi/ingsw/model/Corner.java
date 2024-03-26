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

}
