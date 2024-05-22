package it.polimi.ingsw.controller;

import java.io.Serializable;

/**
 * Record used to represent a game along with all its information.
 * @param gameId the id of the game.
 * @param gameName the name of the game.
 * @param gameStatus the status of the game.
 */
public record GameInfo(int gameId, String gameName, GameStatus gameStatus) implements Serializable {}