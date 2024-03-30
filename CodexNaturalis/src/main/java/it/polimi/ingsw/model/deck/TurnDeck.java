package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.BasicCard;
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
     *
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
     * get the back of the card on top of the stack.
     * this method will be used whenever we'll need to show the deck from the view
     * @return the back side of the card that's on top of the deck
     */
    public BasicCard getCardOnTop(){
        return cards.peek().backSide();
    }
    /**
     * Getter for visible cards
     *
     * @return ArrayList of visible cards
     */
    public ArrayList<CardSides> getVisibleCards(){
        return new ArrayList<>(this.visibleCards);
    }

    /**
     * Returns the visible card selected by index and replaces it with the top card of the deck if it's not empty
     *
     * @param index the index of the selected visible card
     * @return the selected visible card
     */
    public CardSides drawVisibleCard(int index){

        CardSides drawnCard = this.visibleCards.get(index);

        if(this.cards.isEmpty())
            this.visibleCards.remove(index);
        else
            this.visibleCards.set(index, this.cards.pop());

        return drawnCard;
    }
}