package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.function.Function;

/**
 * Generic deck class.
 * @param <T> the type of the object contained inside the deck.
 * @author Guglielmo Gatti
 */

public class GenericDeck <T>{
    Function<Integer, T> builderMethod;
    Stack<T> deck;

    public GenericDeck(Function<Integer,T> builderMethod, int rangeStart, int rangeEnd){
        if (rangeStart > rangeEnd || rangeStart < 0){
            throw new RuntimeException("The supplied value range is not valid");
        }
        this.builderMethod = builderMethod;
        this.deck = new Stack<>();
        for(int i = rangeStart; i <= rangeEnd; i++){
            this.deck.push(builderMethod.apply(i));
        }
        Collections.shuffle(this.deck);
    }

    /**
     * Getter for cards
     * @return ArrayList of cards
     */
    public ArrayList<T> getDeck(){
        return new ArrayList<>(this.deck);
    }

    /**
     * Check whether the deck is empty
     * @return a boolean representing if the deck is empty
     */
    public boolean isEmpty(){
        return this.deck.isEmpty();
    }

    /**
     * Draws a random integer present in the deck and removes it.
     * @return a random integer from the deck
     * @exception EmptyStackException if the deck is empty when the user tries to draw.
     */
    public T draw(){
        return this.deck.pop();
    }
}
