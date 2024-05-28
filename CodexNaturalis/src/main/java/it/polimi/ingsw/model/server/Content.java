package it.polimi.ingsw.model.server;

/**
 * Enum that represents every possible symbol contained in cards.
 * Every color si associated with the respective in-game resource (e.g. Red -> Mushroom); white is used for corners
 * with no symbols and empty for blank ones that can't be overlapped. Lastly there are the three object types.
 */
public enum Content {
    RED("\u001B[41m  \u001B[m","\u001B[31m","#f14624"),
    GREEN("\u001b[42m  \u001b[m", "\u001B[32m", "#2d853a"),
    BLUE("\u001b[44m  \u001b[m", "\u001B[34m", "#5cc7b1"),
    PURPLE("\u001b[45m  \u001b[m", "\u001B[35m", "#8d1a85"),
    WHITE("\u001b[47;1m  \u001b[m", "\u001B[0m", "#FFFFFF"),
    PEN("^^", "\u001B[0m", "#FFFFFF"),
    PAPER("[]", "\u001B[0m", "#FFFFFF"),
    INK("()", "\u001B[0m", "#FFFFFF"),
    EMPTY("\u001b[m  \u001b[m", "\u001B[0m", "#FFFFFF");

    private final String symbol;
    private final String textColorString;
    private final String hexColorString;

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
     * @return the color for each content.
     */
    public String getTextColorString(){
        return textColorString;
    }

    /**
     * @return the hex color string
     */
    public String getHexColorString(){ return hexColorString; }

    /**
     * Constructor for the enum.
     * @param symbol the textual representation for each value of the enum.
     */
    Content(String symbol, String textColorString, String hexColorString){
        this.symbol = symbol;
        this.textColorString = textColorString;
        this.hexColorString = hexColorString;
    }
}