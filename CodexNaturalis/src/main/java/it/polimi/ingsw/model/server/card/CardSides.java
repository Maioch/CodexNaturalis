package it.polimi.ingsw.model.server.card;

/**
 * Record that represents each side of the card.
 * @param frontSide the card's front side.
 * @param backSide the card's back side.
 */
public record CardSides(BasicCard frontSide, BasicCard backSide) {
}