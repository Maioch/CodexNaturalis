package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.CardBuilder;

import java.util.*;


/**
 * Class that represents a generic deck from which the identifying integers for cards can be drawn.
 * Used to represent the starter card deck and the objective deck.
 * the cards are ordered by their id in ascending order
 *
 * @author Guglielmo Gatti
 */
public class Deck {
    Stack<CardSides> cards;

    /**
     * Creates a deck including all card indices from rangeStart to rangeEnd in ascending order
     *
     * @param rangeStart the inclusive index at which the card range of the deck starts
     * @param rangeEnd the inclusive index at which the card range of the deck ends
     * @exception RuntimeException if the given range is invalid
     */
    public Deck(int rangeStart, int rangeEnd){
        if (rangeStart > rangeEnd || rangeStart < 0){
            throw new RuntimeException("The supplied value range is not valid");
        }

        this.cards = new Stack<>();
        for(int i = rangeStart; i <= rangeEnd; i++){
            this.cards.push(CardBuilder.buildCard(i));
        }

        Collections.shuffle(this.cards);
    }

    /**
     * Getter for cards
     *
     * @return ArrayList of cards
     */
    public ArrayList<CardSides> getCards(){
        return new ArrayList<>(this.cards);
    }

    /**
     * Check whether the deck is empty
     *
     * @return a boolean representing if the deck is empty
     */
    public boolean isEmpty(){
        return this.cards.isEmpty();
    }

    /**
     * Draws a random integer present in the deck and removes it.
     *
     * @return a random integer from the deck
     * @exception RuntimeException if the deck is empty when the user tries to draw.
     */
    public CardSides draw(){
        if(this.cards.isEmpty()){
            throw new RuntimeException("There are no more cards available at the requested source");
        }

        return this.cards.pop();
    }
}