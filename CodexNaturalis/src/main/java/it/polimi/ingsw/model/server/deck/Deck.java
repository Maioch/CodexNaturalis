package it.polimi.ingsw.model.server.deck;

import it.polimi.ingsw.exceptions.DeckException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

/**
 * Deck is a stack of randomly sorted objects, used to represent the decks in the game.
 *
 * @param <T> the type of the objects contained inside the deck.
 */

public class Deck<T>{

    Stack<T> deck;

    /**
     * Class constructor.
     *
     * @param factoryMethod     a method that takes an id and creates the corresponding object.
     * @param rangeStart        the id to start generating the deck's objects from.
     * @param rangeEnd          the id to end generating the deck's objects at.
     *
     * @exception DeckException if the given start-end range is illegal.
     */
    public Deck(Function<Integer,T> factoryMethod, int rangeStart, int rangeEnd) throws DeckException{
        if (rangeStart > rangeEnd || rangeStart < 0){
            throw new DeckException("The supplied value range is not valid");
        }
        this.deck = new Stack<>();
        for(int i = rangeStart; i <= rangeEnd; i++){
            this.deck.push(factoryMethod.apply(i));
        }
        Collections.shuffle(this.deck);
    }

    /**
     * Returns all the objects contained in the
     *
     * @return the list of cards in a specified deck.
     */
    public List<T> getDeck(){
        return new ArrayList<>(this.deck);
    }

    /**
     * Check whether the deck is empty.
     * @return true if the deck is empty.
     */
    public boolean isEmpty(){
        return this.deck.isEmpty();
    }

    /**
     * Draws the last card of a specified deck.
     * @return the last card of the deck.
     * @exception DeckException if the deck is empty when the user tries to draw.
     */
    public T draw() throws DeckException{
        if(this.deck.isEmpty())
            throw new DeckException("Called draw on empty deck");
        return this.deck.pop();
    }
}