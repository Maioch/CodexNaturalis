package it.polimi.ingsw.controller;

import java.io.Serializable;

/**
 * GameInfo is a record used to represent a game with its related information.
 *
 * @param gameId     the game's id.
 * @param gameName   the game's name.
 * @param gameStatus the game's status.
 *
 * @see GameStatus
 */
public record GameInfo(int gameId, String gameName, GameStatus gameStatus) implements Serializable {}