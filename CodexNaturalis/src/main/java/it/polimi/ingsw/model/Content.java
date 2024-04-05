package it.polimi.ingsw.model;

/**
 * Enum to represent every possible symbol contained in cards. Every color si associated with the respective
 * resource (e.g. Red -> Mushroom); white is used for corners with no symbols and empty for blank ones that
 * can't be overlapped
 *
 * @author Marco Maiocchi
 */
public enum Content {
    RED, GREEN, BLUE, PURPLE, WHITE, PEN, PAPER, INK, EMPTY;

    /**
     * @return true if this is a color
     */
    public boolean isColor(){
        return this == RED || this == GREEN || this == BLUE || this == PURPLE || this == WHITE;
    }

    /**
     * @return true if this is an object
     */
    public boolean isObject(){
        return this == PEN || this == PAPER || this == INK;
    }

    /**
     * @return true if there's no symbol
     */
    public boolean isEmpty(){
        return this == EMPTY;
    }
}