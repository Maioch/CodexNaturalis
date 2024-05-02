package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Message sent by the server, containing all the possible draw options
 */
public class DrawOptionsMessage extends Message {
    private final HashMap<CardType, ArrayList<BasicCard>> drawableOptions;

    /**
     * Constructor of the class
     * @param drawableOptions hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public DrawOptionsMessage(HashMap<CardType, ArrayList<BasicCard>> drawableOptions) {
        super(Status.DRAW_OPTIONS);
        this.drawableOptions = drawableOptions;
    }

    /**
     * Getter method of the drawable options
     * @return hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public HashMap<CardType, ArrayList<BasicCard>> getDrawableOptions() {
        return new HashMap<>(){{
            for(Map.Entry<CardType, ArrayList<BasicCard>> entry : drawableOptions.entrySet()){
                put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }};
    }
}
