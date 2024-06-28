package it.polimi.ingsw.model.shared.card;

import java.io.Serializable;

/**
 * Represents both sides of each card.
 *
 * @param frontSide the card's front side.
 * @param backSide  the card's back side.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see BasicCard
 */
public record CardSides(BasicCard frontSide, BasicCard backSide) implements Serializable {}