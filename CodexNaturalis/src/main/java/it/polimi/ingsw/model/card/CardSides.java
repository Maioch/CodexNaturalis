package it.polimi.ingsw.model.card;

/**
 * Record to represent the two sides of the cards
 *
 * @param frontSide
 * @param backSide
 */
public record CardSides(BasicCard frontSide, BasicCard backSide) {
}