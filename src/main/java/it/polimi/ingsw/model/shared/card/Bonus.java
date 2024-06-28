package it.polimi.ingsw.model.shared.card;

import it.polimi.ingsw.model.server.Player;

/**
 * Represents the bonus found in gold cards and in objectives.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see GoldCard
 * @see Objective
 */
public interface Bonus {

    /**
     * Computes the amount of points gained by a player after a specific move.
     *
     * @param cardOwner the player who owns the card/objective with the bonus.
     *
     * @return          the points awarded by the bonus.
     */
    int calculate(Player cardOwner);
}