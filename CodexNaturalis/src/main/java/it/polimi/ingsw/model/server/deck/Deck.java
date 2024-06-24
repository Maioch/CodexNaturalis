package it.polimi.ingsw.model.server.deck;

import it.polimi.ingsw.exceptions.DeckException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;

/**
 * Represents a stack of randomly sorted objects, used to represent the decks in the game.
 *
 * @param <T> the type of the objects contained inside the deck.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */

public class Deck<T>{

    //the stack that contains the objects that are part of the deck.
    protected Stack<T> deck;

    /**
     * Class constructor.
     *
     * @param factoryMethod  a method that takes an id and creates the corresponding object.
     * @param rangeStart     the id to start generating the deck's objects from.
     * @param rangeEnd       the id to end generating the deck's objects at.
     *
     * @throws DeckException if the given range is illegal.
     */
    public Deck(Function<Integer,T> factoryMethod, int rangeStart, int rangeEnd) throws DeckException{
        if (rangeStart > rangeEnd || rangeStart <= 0){
            throw new DeckException("The supplied value range is not valid");
        }
        this.deck = new Stack<>();
        for(int i = rangeStart; i <= rangeEnd; i++){
            this.deck.push(factoryMethod.apply(i));
        }
        Collections.shuffle(this.deck);
    }

    /**
     * Gets all the objects contained in the deck.
     *
     * @return the list of cards in the deck.
     */
    public List<T> getDeck(){
        return new ArrayList<>(this.deck);
    }

    /**
     * Checks whether the deck is empty or not.
     *
     * @return true if the deck is empty.
     */
    public boolean isEmpty(){
        return this.deck.isEmpty();
    }

    /**
     * Draws the top card of this deck.
     *
     * @return               the top card of the deck.
     *
     * @throws DeckException if the deck is empty when the user tries to draw.
     */
    public T draw() throws DeckException{
        if(this.deck.isEmpty()) {
            throw new DeckException("Called draw on empty deck");
        }
        return this.deck.pop();
    }
}