package it.polimi.ingsw.view;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.model.shared.Content;

import java.util.List;


/**
 * SetupView is a generic interface containing all the headers of the methods used by both the CLI and GUI during
 * the setup phase of the application.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public interface SetupView extends ReconnectableView{

    /**
     * Updates the list of all matches that have already been created on the server to which the client is connected.
     *
     * @param matchList the list that contains all the available matches.
     *
     * @see GameInfo
     *
     */
    void updateMatchList(List<GameInfo> matchList);

    /**
     * Notifies that a new game has been created and requests the list of the colors that can be chosen by the players.
     *
     * @param gameId the ID of the new game.
     */
    void newGameSuccess(int gameId);

    /**
     * Allows a custom error message to be printed, following a critical error.
     * An error is "critical" when the client must be returned to the match selection interface, after its occurrence.
     *
     * @param message the custom message.
     */
    void showCriticalError(String message);

    /**
     * Handles the joining phase to a game.
     * To access a game, the client must choose a unique nickname and a color from the list of the available ones.
     *
     * @param colors the list of available colors.
     * @param gameId the ID of the game the client is joining.
     */
    void showJoinGameDialog(List<Content> colors, int gameId);

    /**
     * Allows a custom error message to be printed, following a non-critical error.
     * Used when the client chooses an invalid nickname or color.
     *
     * @param message the custom message.
     * @param gameId  the ID of the game the client is joining.
     */
    void showUserError(String message, int gameId);

    /**
     * Notifies that the client successfully joined a game.
     *
     * @param nickname        the nickname chosen by the player.
     * @param color           the color chosen by the player.
     * @param numberOfPlayers the number of players that can join the game.
     */
    void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers);

    /**
     * Handles the disconnection of the client.
     * When the disconnection occurs, the client is returned to the match selection interface.
     *
     * @param message the message printed when a client disconnects.
     */
    void showReconnectionError(String message);

}