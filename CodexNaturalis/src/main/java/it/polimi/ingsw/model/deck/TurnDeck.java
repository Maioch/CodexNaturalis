package it.polimi.ingsw.model.deck;

import java.util.ArrayList;

/**
 * Class that represents gold and resource decks. It also represents the visible cards beside the deck
 *
 * @author Andrea Fidanza
 */
public class TurnDeck extends Deck {
    private final ArrayList<Integer> visibleCards;

    /**
     * Class constructor. Gets the visible cards from the top of the deck
     *
     * @param range_start the inclusive index at which the card range of the deck starts
     * @param range_end the inclusive index at which the card range of the deck ends
     * @param numberOfVisibleCards the size of the array of visible cards
     * @exception InvalidRangeException if numberOfVisibleCards is greater than the number of cards in the deck
     */
    public TurnDeck(int range_start, int range_end, int numberOfVisibleCards){
        super(range_start, range_end);
        if(numberOfVisibleCards > range_end - range_start)
            throw new InvalidRangeException();
        visibleCards = new ArrayList<>(numberOfVisibleCards);
        for(int i = 0; i < numberOfVisibleCards && !this.isEmpty(); i++)
            visibleCards.add(cards.removeLast());
    }

    /**
     * Getter for visible cards
     *
     * @return ArrayList of visible cards
     */
    public ArrayList<Integer> getVisibleCards(){
        return (ArrayList<Integer>) visibleCards.clone();
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     *
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public int drawVisibleCard(int index) throws NoMoreCardsException{
        if(visibleCards.get(index) == null)
            throw new NoMoreCardsException();
        int drawnCard = visibleCards.get(index);
        if(cards.isEmpty())
            visibleCards.remove(index);
        else
            visibleCards.set(index, cards.removeLast());
        return drawnCard;
    }
}