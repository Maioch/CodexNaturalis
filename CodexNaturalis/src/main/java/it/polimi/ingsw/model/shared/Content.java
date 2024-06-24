package it.polimi.ingsw.model.shared;

/**
 * Represents every possible symbol contained in cards.
 * Every color si associated with the respective in-game resource (for example: Red -> Mushroom); white is used for
 * corners with no symbols and empty for blank ones that can't be overlapped. Lastly there are the three object types.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public enum Content {

    /**
     * the red resource type and player color, also known as the fungi kingdom.
     */
    RED("\u001B[41m  \u001B[m","\u001B[31m","#f14624"),
    /**
     * the green resource type and player color, also known as the plant kingdom.
     */
    GREEN("\u001b[42m  \u001b[m", "\u001B[32m", "#2d853a"),
    /**
     * the blue resource type and player color, also known as the animal kingdom.
     */
    BLUE("\u001b[44m  \u001b[m", "\u001B[34m", "#5cc7b1"),
    /**
     * the purple resource type and player color, also known as the insect kingdom.
     */
    PURPLE("\u001b[45m  \u001b[m", "\u001B[35m", "#8d1a85"),
    /**
     * the white content type, used to represent cards that don't have a specific color, like starter cards,
     * and corners upon which cards can be placed that don't have any resources or objects.
     */
    WHITE("\u001b[47;1m  \u001b[m", "\u001B[0m", "#FFFFFF"),
    /**
     * the pen object type.
     */
    PEN("^^", "\u001B[0m", "#FFFFFF"),
    /**
     * the paper object type.
     */
    PAPER("[]", "\u001B[0m", "#FFFFFF"),
    /**
     * the ink object type.
     */
    INK("()", "\u001B[0m", "#FFFFFF"),
    /**
     * the content type used to represent corners upon which cards can't be placed.
     */
    EMPTY("\u001b[m  \u001b[m", "\u001B[0m", "#FFFFFF");

    private final String symbol;
    private final String textColorString;
    private final String hexColorString;

    /**
     * Checks if this content is a color.
     *
     * @return true if this is a color
     */
    public boolean isColor(){
        return this == RED || this == GREEN || this == BLUE || this == PURPLE || this == WHITE;
    }

    /**
     * Checks if this content is a resource.
     *
     * @return true if this is a resource
     */
    public boolean isResource(){
        return this == RED || this == GREEN || this == BLUE || this == PURPLE;
    }

    /**
     * Checks if this content is an object.
     *
     * @return true if this is an object
     */
    public boolean isObject(){
        return this == PEN || this == PAPER || this == INK;
    }

    /**
     * Checks if this content is empty, meaning that the corner it refers to cannot be overlapped.
     *
     * @return true if this is empty.
     */
    public boolean isEmpty(){
        return this == EMPTY;
    }

    /**
     * Gets the emote associated to this content.
     *
     * @return the content's symbol.
     */
    public String getSymbol(){
        return symbol;
    }

    /**
     * Gets the text color associated to this content.
     *
     * @return the color for each content.
     */
    public String getTextColorString(){
        return textColorString;
    }

    /**
     * Gets the hexadecimal representation of the text color associated to this content.
     *
     * @return the hex color string
     */
    public String getHexColorString(){ return hexColorString; }

    /**
     * Enum constructor.
     *
     * @param symbol          the content's emote.
     * @param textColorString the content's text color.
     * @param hexColorString  the content's color hexadecimal string.
     */
    Content(String symbol, String textColorString, String hexColorString){
        this.symbol = symbol;
        this.textColorString = textColorString;
        this.hexColorString = hexColorString;
    }
}