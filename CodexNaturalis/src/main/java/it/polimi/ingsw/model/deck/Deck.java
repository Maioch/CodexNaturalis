package it.polimi.ingsw.model.deck;

import java.util.ArrayList;
import java.util.Random;


/**
 * Class that represents a generic deck from which the identifying integers for cards can be drawn.
 * Used to represent the starter card deck and the objective deck.
 * the cards are ordered by their id in ascending order
 *
 * @author Guglielmo Gatti
 */
public class Deck {
    private final Random random;
    ArrayList<Integer> cards;

    /**
     * Creates a deck including all card indices from rangeStart to rangeEnd in ascending order
     *
     * @param rangeStart the inclusive index at which the card range of the deck starts
     * @param rangeEnd the inclusive index at which the card range of the deck ends
     */
    public Deck(int rangeStart, int rangeEnd){
        cards = new ArrayList<Integer>();
        random = new Random();
        if (rangeStart > rangeEnd || rangeStart < 0){
            throw new InvalidRangeException();
        }
        for (int i = rangeStart; i <= rangeEnd; i++){
            this.cards.add(i);
        }
    }

    /**
     * Getter for cards
     *
     * @return ArrayList of cards
     */
    public ArrayList<Integer> getCards(){
        return (ArrayList<Integer>)cards.clone();
    }

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
    public int draw(){
        if(cards.isEmpty()){
            throw new NoMoreCardsException();
        }
        int randomIndex = random.nextInt(cards.size());
        return cards.remove(randomIndex);
    }

    public static class NoMoreCardsException extends RuntimeException{
        @Override
        public String getMessage(){
            return "There are no more cards available at the requested source";
        }
    }

    public static class InvalidRangeException extends RuntimeException{
        @Override
        public String getMessage(){
            return "The supplied value range is not valid";
        }
    }
}
