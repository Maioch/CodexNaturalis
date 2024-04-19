package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.CardSides;

import java.util.ArrayList;
import java.util.function.Function;

public class GenericTurnDeck<T> extends GenericDeck<T>{
    private final ArrayList<T> visibleElements;

    public GenericTurnDeck(Function<Integer,T> builderMethod, int rangeStart, int rangeEnd, int numberOfVisibleCards) {
        super(builderMethod, rangeStart, rangeEnd);
        if(numberOfVisibleCards > rangeEnd - rangeStart)
            throw new RuntimeException("The supplied value range is not valid");
        this.visibleElements = new ArrayList<>(numberOfVisibleCards);
        for(int i = 0; i < numberOfVisibleCards; i++)
            this.visibleElements.add(this.deck.pop());
    }

    /**
     * Gets the card on top of the stack.
     * This method will be used whenever we'll need to show the deck from the view
     * @return the card that's on top of the deck
     */
    public T getCardOnTop(){
        return deck.peek();
    }

    /**
     * Getter for visible cards
     * @return ArrayList of visible cards
     */
    public ArrayList<T> getVisibleElements(){
        return new ArrayList<>(this.visibleElements);
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public T drawVisibleElement(int index){
        try {
            T drawnCard = this.visibleElements.get(index);
            if (this.deck.isEmpty())
                this.visibleElements.remove(index);
            else
                this.visibleElements.set(index, this.deck.pop());
            return drawnCard;
        }
        catch (IndexOutOfBoundsException e){
            throw new RuntimeException(
                    String.format("Attempted to draw a visible card, but the supplied index, %d, was out of bounds",index));
        }
    }

    /**
     * Overridden isEmpty method
     * @return true if both the array of visible cards and the deck are empty
     */
    @Override
    public boolean isEmpty(){
        return this.visibleElements.isEmpty() && this.deck.isEmpty();
    }

}

