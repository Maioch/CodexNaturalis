package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.CardSides;

import java.util.ArrayList;

/**
 * Class that represents gold and resource decks. It also represents the visible cards beside the deck
 *
 * @author Andrea Fidanza, Guglielmo Gatti
 */
public class TurnDeck extends Deck {
    private final ArrayList<CardSides> visibleCards;

    /**
     * Class constructor. Gets the visible cards from the top of the deck
     * @param range_start the inclusive index at which the card range of the deck starts
     * @param range_end the inclusive index at which the card range of the deck ends
     * @param numberOfVisibleCards the size of the array of visible cards
     * @exception RuntimeException if numberOfVisibleCards is greater than the number of cards in the deck
     */
    public TurnDeck(int range_start, int range_end, int numberOfVisibleCards){
        super(range_start, range_end);
        if(numberOfVisibleCards > range_end - range_start)
            throw new RuntimeException("The supplied value range is not valid");
        this.visibleCards = new ArrayList<>(numberOfVisibleCards);
        for(int i = 0; i < numberOfVisibleCards; i++)
            this.visibleCards.add(this.cards.pop());
    }

    /**
     * Gets the card on top of the stack.
     * This method will be used whenever we'll need to show the deck from the view
     * @return the card that's on top of the deck
     */
    public CardSides getCardOnTop(){
        return cards.peek();
    }

    /**
     * Getter for visible cards
     * @return ArrayList of visible cards
     */
    public ArrayList<CardSides> getVisibleCards(){
        return new ArrayList<>(this.visibleCards);
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public CardSides drawVisibleCard(int index){
        try {
            CardSides drawnCard = this.visibleCards.get(index);
            if (this.cards.isEmpty())
                this.visibleCards.remove(index);
            else
                this.visibleCards.set(index, this.cards.pop());
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
        return this.visibleCards.isEmpty() && this.cards.isEmpty();
    }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object){
        if(this.getClass() != object.getClass())
            return false;
        TurnDeck other = (TurnDeck) object;
        ArrayList<CardSides> allCards = new ArrayList<>(){{
            addAll(cards);
            addAll(visibleCards);
        }};
        ArrayList<CardSides> allOtherCards = new ArrayList<>(){{
            addAll(other.cards);
            addAll(other.visibleCards);
        }};
        return allCards.containsAll(allOtherCards) &&
                allOtherCards.containsAll(allCards) &&
                this.visibleCards.size() == other.visibleCards.size();
    }
}