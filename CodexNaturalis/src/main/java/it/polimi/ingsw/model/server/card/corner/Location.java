package it.polimi.ingsw.model.server.card.corner;

/**
 * Location represents the position of the corner relative to the card (for example: BL -> bottom left, TR -> top right).
 */
public enum Location {

    BL,
    BR,
    TL,
    TR;

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
}