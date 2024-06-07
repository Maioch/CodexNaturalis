package it.polimi.ingsw.model.server.card.corner;

/**
 * Location represents the position of the corner relative to the card (for example: BL -> bottom left, TR -> top right).
 * Every location value has its offset coordinates embedded.
 */
public enum Location {

    BL(0, 0),
    BR(1, 0),
    TL(0, 1),
    TR(1, 1);

    private final int x;
    private final int y;

    Location(int x, int y){
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the symmetrical location to the given one in relation to the card's diagonal.
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
     * @return the x coordinate relative to the card bottom left corner.
     */
    public int getX(){
        return x;
    }

    /**
     * @return the y coordinate relative to the card bottom left corner.
     */
    public int getY(){
        return y;
    }
}