package it.polimi.ingsw.model.server.card;

import java.io.Serializable;

/**
 * Record that represents each side of the card.
 * @param frontSide the card's front side.
 * @param backSide the card's back side.
 */
public record CardSides(BasicCard frontSide, BasicCard backSide) implements Serializable {
}