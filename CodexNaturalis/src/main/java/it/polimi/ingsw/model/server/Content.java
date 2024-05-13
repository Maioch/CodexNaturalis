package it.polimi.ingsw.model.server;

/**
 * Enum that represents every possible symbol contained in cards. Every color si associated with the respective in-game
 * resource (e.g. Red -> Mushroom); white is used for corners with no symbols and empty for blank ones that
 * can't be overlapped. Lastly there are the three object types.
 *
 * @author Marco Maiocchi
 */
public enum Content {
    RED("\u001B[41m  \u001B[m"),
    GREEN("\u001b[42m  \u001b[m"),
    BLUE("\u001b[44m  \u001b[m"),
    PURPLE("\u001b[45m  \u001b[m"),
    WHITE("\u001b[47;1m  \u001b[m"),
    PEN("^^"),
    PAPER("[]"),
    INK("()"),
    EMPTY("\u001b[m  \u001b[m");

    private final String symbol;

    /**
     * @return true if this is a color
     */
    public boolean isColor(){
        return this == RED || this == GREEN || this == BLUE || this == PURPLE || this == WHITE;
    }

    /**
     * @return true if this is a resource
     */
    public boolean isResource(){
        return this == RED || this == GREEN || this == BLUE || this == PURPLE;
    }

    /**
     * @return true if this is an object
     */
    public boolean isObject(){
        return this == PEN || this == PAPER || this == INK;
    }

    /**
     * @return true if a corner can't be overlapped.
     */
    public boolean isEmpty(){
        return this == EMPTY;
    }

    /**
     * @return the symbol.
     */
    public String getSymbol(){
        return symbol;
    }

    /**
     * Constructor for the enum.
     * @param symbol the textual representation for each value of the enum.
     */
    Content(String symbol){
        this.symbol = symbol;
    }
}