package it.polimi.ingsw.model.deck;

import java.util.ArrayList;
import java.util.Objects;

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
     */
    public TurnDeck(int range_start, int range_end, int numberOfVisibleCards){
        super(range_start, range_end);
        visibleCards = new ArrayList<Integer>(numberOfVisibleCards);
        for(int i = 0; i < numberOfVisibleCards; i++)
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
        visibleCards.set(index, cards.isEmpty() ? null : cards.removeLast());
        return drawnCard;
    }

    /**
     * Returns all the indexes that correspond to a visible card
     *
     * @return the valid indexes array
     */
    public ArrayList<Integer> getValidIndexes(){
        return visibleCards.stream().filter(Objects::nonNull).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}