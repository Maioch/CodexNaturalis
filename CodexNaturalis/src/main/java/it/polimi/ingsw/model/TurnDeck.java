package it.polimi.ingsw.model;

/**
 * Class that represents gold and resource decks. It also represents the visible cards beside the deck
 *
 * @author Andrea Fidanza
 */
public class TurnDeck extends Deck{
    private final int numberOfVisibleCards = 2;
    private final Integer[] visibleCards = new Integer[numberOfVisibleCards];

    /**
     * Class constructor. Gets the visible cards from the top of the deck
     *
     * @param range_start the inclusive index at which the card range of the deck starts
     * @param range_end the inclusive index at which the card range of the deck ends
     */
    public TurnDeck(int range_start, int range_end){
        super(range_start, range_end);
        for(int i = 0; i < numberOfVisibleCards; i++)
            visibleCards[i] = cards.removeLast();
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     *
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public int drawVisibleCard(int index) throws NoMoreCardsException{
        if(visibleCards[index] == null)
            throw new NoMoreCardsException();
        int drawnCard = visibleCards[index];
        visibleCards[index] = (cards.isEmpty()) ? null : cards.removeLast();
        return drawnCard;
    }
}