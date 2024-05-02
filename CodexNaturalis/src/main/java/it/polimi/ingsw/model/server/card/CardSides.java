package it.polimi.ingsw.model.server.card;

/**
 * Record that represents each side of the card, front and back.
 * @param frontSide
 * @param backSide
 */
public record CardSides(BasicCard frontSide, BasicCard backSide) {
}