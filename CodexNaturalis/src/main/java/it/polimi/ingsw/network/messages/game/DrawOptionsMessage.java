package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message sent by the server, containing all the possible draw options
 */
public class DrawOptionsMessage extends Message {
    private final Map<CardType, List<BasicCard>> drawableOptions;

    /**
     * Constructor of the class
     * @param drawableOptions hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public DrawOptionsMessage(Status status, Map<CardType, List<BasicCard>> drawableOptions) {
        super(status);
        this.drawableOptions = new HashMap<>(drawableOptions);
    }

    /**
     * Getter method of the drawable options
     * @return hashmap containing, respectively, the card type and the card (sides) itself of all the possible options
     */
    public Map<CardType, List<BasicCard>> getDrawableOptions() {
        return new HashMap<>(){{
            for(Map.Entry<CardType, List<BasicCard>> entry : drawableOptions.entrySet()){
                put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }};
    }
}
