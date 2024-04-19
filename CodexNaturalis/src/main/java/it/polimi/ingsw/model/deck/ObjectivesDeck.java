package it.polimi.ingsw.model.deck;

import it.polimi.ingsw.model.card.CardBuilder;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.Objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Stack;

public class ObjectivesDeck {
    Stack<Objective> objectives;

    /**
     * Creates a deck including all card indices from rangeStart to rangeEnd in ascending order
     * @param rangeStart the inclusive index at which the card range of the deck starts
     * @param rangeEnd the inclusive index at which the card range of the deck ends
     * @exception RuntimeException if the given range is invalid
     */
    public ObjectivesDeck(int rangeStart, int rangeEnd){
        if (rangeStart > rangeEnd || rangeStart < 0){
            throw new RuntimeException("The supplied value range is not valid");
        }
        this.objectives = new Stack<>();
        for(int i = rangeStart; i <= rangeEnd; i++){
            this.objectives.push(CardBuilder.buildObjective(i));
        }
        Collections.shuffle(this.objectives);
    }

    /**
     * Getter for cards
     * @return ArrayList of cards
     */
    public ArrayList<Objective> getObjectives(){
        return new ArrayList<>(this.objectives);
    }

    /**
     * Check whether the deck is empty
     * @return a boolean representing if the deck is empty
     */
    public boolean isEmpty(){
        return this.objectives.isEmpty();
    }

    /**
     * Draws a random integer present in the deck and removes it.
     * @return a random integer from the deck
     * @exception EmptyStackException if the deck is empty when the user tries to draw.
     */
    public Objective draw(){
        return this.objectives.pop();
    }

    /**
     * Equals method.
     * @param object Object to check
     * @return true if each field is equals to each field of object
     */
    @Override
    public boolean equals(Object object){
        if(this.getClass() != object.getClass())
            return false;
        ObjectivesDeck other = (ObjectivesDeck) object;
        return this.objectives.stream().sorted().toList()
                .equals(other.objectives.stream().sorted().toList());
    }
}
