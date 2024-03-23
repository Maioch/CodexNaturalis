package it.polimi.ingsw.model;

/**
 * Enum to represent every possible symbol contained in cards. Every color si associated with the respective
 * resource (e.g. Red -> Mushroom); white is needed to represent starter cards.
 *
 * @author Marco Maiocchi
 */
public enum Content {
    RED, GREEN, BLUE, PURPLE, WHITE, PEN, PAPER, INK;

    /**
     * @param content symbol to check
     * @return true if content is a color
     */
    public boolean isColor(Content content){
        return content == RED || content == GREEN || content == BLUE || content == PURPLE || content == WHITE;
    }

    /**
     * @param content symbol to check
     * @return true if content is an object
     */
    public boolean isObject(Content content){
        return !isColor(content);
    }
}