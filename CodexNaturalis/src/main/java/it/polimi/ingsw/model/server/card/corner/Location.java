package it.polimi.ingsw.model.server.card.corner;

/**
 * Enum that represents corner locations in a card (e.g. BL -> bottom left).
 *
 * @author Andrea Fidanza
 */
public enum Location {
    BL, BR, TL, TR;

    /**
     * @return the opposite location (symmetrical with respect to the diagonal of the card).
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