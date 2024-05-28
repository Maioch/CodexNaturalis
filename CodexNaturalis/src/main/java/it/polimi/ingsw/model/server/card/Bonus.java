package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.model.server.Player;

/**
 * Bonus is an interface needed to introduce a method that computes the amount of points gained by a player after
 * a specific move.
 */
public interface Bonus {

    int calculate(Player cardOwner);
}