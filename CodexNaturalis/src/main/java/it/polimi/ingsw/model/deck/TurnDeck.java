package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.BasicCard;

import java.util.ArrayList;

/**
 * Class that represents gold and resource decks. It also represents the visible cards beside the deck
 *
 * @author Andrea Fidanza
 */
public class TurnDeck extends Deck {
    private final ArrayList<BasicCard> visibleCards;

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

        this.visibleCards = new ArrayList<>(numberOfVisibleCards);
        for(int i = 0; i < numberOfVisibleCards; i++)
            this.visibleCards.add(this.cards.pop());
    }

    /**
     * Getter for visible cards
     *
     * @return ArrayList of visible cards
     */
    public ArrayList<BasicCard> getVisibleCards(){
        return new ArrayList<>(this.visibleCards);
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     *
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public BasicCard drawVisibleCard(int index) throws NoMoreCardsException{
        if(this.visibleCards.get(index) == null)
            throw new NoMoreCardsException();

        BasicCard drawnCard = this.visibleCards.get(index);

        if(this.cards.isEmpty())
            this.visibleCards.remove(index);
        else
            this.visibleCards.set(index, this.cards.pop());

        return drawnCard;
    }
}