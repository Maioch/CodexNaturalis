package it.polimi.ingsw.model.server.card;

import it.polimi.ingsw.model.server.Player;

/**
 * Interface that introduces a method to calculate bonus points during different phases of the game.
 *
 * @author Andrea Fidanza
 */
public interface Bonus {
    int calculate(Player cardOwner);
}