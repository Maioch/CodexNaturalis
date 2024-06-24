package it.polimi.ingsw.model.shared.card.corner;

/**
 * Location represents the position of the corner relative to the card (for example: BL -> bottom left, TR -> top right).
 * Every location value has its offset coordinates embedded.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public enum Location {

    /**
     * the bottom left corner of a card.
     */
    BL(0, 0),

    /**
     * the bottom right corner of a card.
     */
    BR(1, 0),

    /**
     * the top left corner of a card.
     */
    TL(0, 1),

    /**
     * the top right corner of a card.
     */
    TR(1, 1);

    private final int x;
    private final int y;

    /**
     * Enum constructor.
     *
     * @param x the x coordinate relative to the bottom left corner of the card.
     * @param y the y coordinate relative to the bottom left corner of the card.
     */
    Location(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Gets the symmetrical location to the given one in relation to the card's centre.
     *
     * @return the opposite location to the given one.
     */
    public Location getOppositeLocation(){
        return switch(this){
            case BL -> Location.TR;
            case BR -> Location.TL;
            case TL -> Location.BR;
            case TR -> Location.BL;
        };
    }

    /**
     * Gets the x coordinate.
     *
     * @return the x coordinate relative to the card bottom left corner.
     */
    public int getX(){
        return x;
    }

    /**
     * Gets the y coordinate.
     *
     * @return the y coordinate relative to the card bottom left corner.
     */
    public int getY(){
        return y;
    }
}