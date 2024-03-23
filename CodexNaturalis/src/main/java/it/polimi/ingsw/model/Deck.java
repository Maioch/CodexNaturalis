package it.polimi.ingsw.model;

import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;


/**
 * Class that represents a generic deck from which the identifying integers for cards can be drawn.
 * Used to represent the starter card deck and the objective deck.
 * the cards are ordered by their id in ascending order
 *
 * @author Guglielmo Gatti
 */
public class Deck {
    private final Random random = new Random();
    protected ArrayList<Integer> cards = new ArrayList<Integer>();


    /**
     * Check whether the deck is empty
     *
     * @return a boolean representing if the deck is empty
     */
    public boolean isEmpty(){
        return cards.isEmpty();
    }

    /**
     * Draws a random integer present in the deck and removes it.
     *
     * @return a random integer from the deck
     * @exception NoMoreCardsException if the deck is empty when the user tries to draw.
     */
    public int draw() throws NoMoreCardsException{
        if(cards.isEmpty()){
            throw new NoMoreCardsException();
        }
        int randomIndex = cards.getFirst() + random.nextInt(cards.getLast() + 1 - cards.getFirst());
        return cards.remove(randomIndex);
    }

    /**
     * Creates a deck including all card indices from rangeStart to rangeEnd in ascending order
     *
     * @param rangeStart the inclusive index at which the card range of the deck starts
     * @param rangeEnd the inclusive index at which the card range of the deck ends
     */
    public Deck(int rangeStart, int rangeEnd){
        for (int i = rangeStart; i <= rangeEnd; i++){
            this.cards.add(i);
        }
    }

    public static class NoMoreCardsException extends Exception{
        @Override
        public String toString(){
            return "There are no more cards available at the requested source";
        }
    }
}
