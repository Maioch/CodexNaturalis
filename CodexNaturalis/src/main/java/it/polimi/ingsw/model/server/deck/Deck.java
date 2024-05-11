package it.polimi.ingsw.model.server.deck;

import it.polimi.ingsw.exceptions.DeckException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

/**
 * Class that represents a stack of randomly sorted objects that have been created
 * through the factory method given to the constructor.
 *
 * @param <T> the type of the object contained inside the deck.
 * @author Guglielmo Gatti, Andrea Fidanza
 */

public class Deck<T>{
    Stack<T> deck;

    /**
     * Constructor for the class.
     * @param factoryMethod a method that takes an id and creates the corresponding object.
     * @param rangeStart the id to start generating the deck's objects from.
     * @param rangeEnd the id to end generating the deck's objects at.
     * @exception DeckException if the given range is illegal.
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