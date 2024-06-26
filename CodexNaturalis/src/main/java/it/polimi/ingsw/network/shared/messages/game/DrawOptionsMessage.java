package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message sent to show the drawable cards.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class DrawOptionsMessage extends Message {
    private final Map<CardType, List<BasicCard>> drawableOptions;
    private final Map<CardType, Integer> numberOfCardsLeft;

    /**
     * Constructor for the class.
     *
     * @param status            the message status
     * @param drawableOptions   a map containing, respectively, the card type and the card (sides)
     *                          itself of all the possible options.
     * @param numberOfCardsLeft a map containing, respectively, the card type and the number of cards
     *                          left for the respective deck.
     *
     * @see Status
     * @see CardType
     * @see BasicCard
     */
    public DrawOptionsMessage(Status status, Map<CardType, List<BasicCard>> drawableOptions, Map<CardType, Integer> numberOfCardsLeft) {
        super(status);
        this.numberOfCardsLeft = numberOfCardsLeft;
        this.drawableOptions = new HashMap<>();
        for(CardType key : drawableOptions.keySet()){
            this.drawableOptions.put(key,new ArrayList<>(drawableOptions.get(key)));
        }
    }

    /**
     * Gets the attached draw options.
     *
     * @return a map containing, respectively, the card type and the card (sides) itself of all the possible options.
     *
     * @see CardType
     * @see BasicCard
     */
    public Map<CardType, List<BasicCard>> getDrawableOptions() {
        return new HashMap<>(){{
            for(Map.Entry<CardType, List<BasicCard>> entry : drawableOptions.entrySet()){
                put(entry.getKey(), new ArrayList<>(entry.getValue()));
            }
        }};
    }

    /**
     * Gets the attached number of cards left.
     *
     * @return a map containing, respectively, the card type and the number of cards left for the respective deck.
     *
     * @see CardType
     */
    public Map<CardType, Integer> getNumberOfCardsLeft() {
        return new HashMap<>(numberOfCardsLeft);
    }
}