package it.polimi.ingsw.model.server.deck;

import it.polimi.ingsw.exceptions.DeckException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * An extension of Deck which adds a variable number of objects that can be revealed without
 * drawing them from the deck.
 * the visible objects are considered separate from the main stack and calling isEmpty will only inform you
 * of the status of the latter.
 *
 * @param <T> the type of the objects contained inside the deck
 * @author Andrea Fidanza, Guglielmo Gatti
 */
public class TurnDeck<T> extends Deck<T> {
    private final List<T> visibleElements;

    /**
     * Constructor for TurnDeck
     *
     * @param factoryMethod a method that takes an id and creates the corresponding object
     * @param rangeStart the id to start generating the deck's objects from
     * @param rangeEnd the id to end generating the deck's objects at
     * @param numberOfVisibleElements the number of visible elements
     * @exception DeckException if the given range is illegal
     */
    public TurnDeck(
            Function<Integer,T> factoryMethod,
            int rangeStart,
            int rangeEnd,
            int numberOfVisibleElements)
            throws DeckException {
        super(factoryMethod, rangeStart, rangeEnd);
        if(numberOfVisibleElements > rangeEnd - rangeStart)
            throw new DeckException("The supplied value range is not valid");
        this.visibleElements = new ArrayList<>(numberOfVisibleElements);
        for(int i = 0; i < numberOfVisibleElements; i++)
            this.visibleElements.add(this.deck.pop());
    }

    /**
     * This method will be used whenever we'll need to show the deck from the view
     * @return the card that's on top of the deck
     */
    public T getElementOnTop(){
        return deck.peek();
    }

    /**
     * @return ArrayList of visible cards
     */
    public List<T> getVisibleElements(){
        return new ArrayList<>(this.visibleElements);
    }

    /**
     * Returns the visible card selected by index and replaces it with the top
     * card of the deck if it's not empty
     * @param index the index of the selected visible card
     * @exception DeckException if the given index is illegal
     * @return the selected visible card
     */
    public T drawVisibleElement(int index) throws DeckException{
        try {
            T drawnCard = this.visibleElements.get(index);
            if (this.deck.isEmpty())
                this.visibleElements.remove(index);
            else
                this.visibleElements.set(index, this.deck.pop());
            return drawnCard;
        }
        catch (IndexOutOfBoundsException e){
            throw new DeckException("The given index does not correspond to any card");
        }
    }
}