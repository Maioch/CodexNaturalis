package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.card.CardSides;
import it.polimi.ingsw.server.model.card.CardType;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Message sent by the server, containing all the possible draw options
 */
public class DrawOptionsMessage extends Message{
    private final HashMap<CardType, ArrayList<CardSides>> drawableOptions;

    /**
     * Constructor of the class
     * @param drawableOptions hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public DrawOptionsMessage(HashMap<CardType, ArrayList<CardSides>> drawableOptions) {
        super(Status.DRAW_OPTIONS);
        this.drawableOptions = drawableOptions;
    }

    /**
     * Getter method of the drawable options
     * @return hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public HashMap<CardType, ArrayList<CardSides>> getDrawableOptions() {
        return drawableOptions;
    }
}
