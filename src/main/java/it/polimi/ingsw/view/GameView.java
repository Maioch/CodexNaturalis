package it.polimi.ingsw.view;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.shared.messages.game.ChatMessage;

import java.util.List;
import java.util.Map;

/**
 * Contains all the headers of the methods used by both CLI and GUI during the actual gameplay.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public interface GameView extends ReconnectableView{

    /**
     * Shows a message indicating that the turn players are going to play next, will also be the last.
     */
    void notifyLastTurn();

    /**
     * Requests a draw selection from the player.
     * In order to let the player choose, this method also shows the list drawable cards.
     *
     * @param drawableCards     the map linking a CardType to its drawable BasicCards.
     * @param numberOfCardsLeft the number of card left in each deck.
     *
     * @see CardType
     * @see BasicCard
     */
    void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft);

    /**
     * Shows the given received chat message.
     *
     * @param chatMessage the message properties: sender, recipients and message.
     *
     * @see ChatMessage
     */
    void showChatMessage(ChatMessage chatMessage);

    /**
     * Requests a placement selection from the player.
     * In order to let the player choose, this method also shows the list of his hand front and back cards.
     *
     * @param handCards     the list of CardSides representing the current player's hand. Each element represents a hand card.
     * @param placedCards   the list of placed cards constituting the player's board.
     *
     * @see CardSides
     * @see BasicCard
     */
    void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards);

    /**
     * Changes the current turn owner and notifies the player of that.
     *
     * @param turnOwner the nickname of the player who'll be the new turn owner.
     */
    void turnChanged(String turnOwner);

    /**
     * Shows the user that an error has occurred
     *
     * @param message the string containing the error message to show.
     */
    void showErrorMessage(String message);

    /**
     * Notifies the player that a player has joined the game.
     * This method runs in the lobby phase, before the starting of the game.
     *
     * @param nickname      the nickname of the newly joined player.
     * @param color         the color of the newly joined player.
     * @param isGameFull    the boolean that, if set true, lets also the game to start.
     *
     * @see Content
     */
    void showUserJoined(String nickname, Content color, boolean isGameFull);

    /**
     * Updates a remote player hand.
     * From the point of view of the local player, the hand cards of the opponents are composed only by back sides.
     *
     * @param nickname  the nickname of the player on which the update is going to be done.
     * @param handCards the new hand cards of the specified remote player. This is a list of BasicCards.
     *
     * @see BasicCard
     */
    void updateRemotePlayerHand(String nickname, List<BasicCard> handCards);

    /**
     * Updates the local player hand.
     * Unlike the updateRemotePlayerHand method, this contains all the card sides of the player's hand.
     *
     * @param handCards the new hand cards of the local player. This is a list of CardSides.
     *
     * @see CardSides
     */
    void updateLocalPlayerHand(List<CardSides> handCards);

    /**
     * Requests a selection of the starter side (either front or back) from the player.
     * In order to let the player choose, this method also shows each side of its starter card.
     *
     * @param playerCards the starting hand cards of the player.
     *                    This is the only point in the game where the player has one more card than normal.
     *
     * @see CardSides
     */
    void requestStarterSide(List<CardSides> playerCards);

    /**
     * Updates the board of a specified player.
     * The board, is a list of BasicCards which were previously chose by the player during the game proceedings.
     * This method also updates the specified player score.
     *
     * @param nickname    the nickname of the player whose board is going to be updated.
     * @param placedCards the list of placed cards of the specified player.
     * @param score       the specified player's new score.
     *
     * @see BasicCard
     */
    void updateBoard(String nickname, List<BasicCard> placedCards, int score);

    /**
     * Requests the personal objective selection from the player.
     * In order to let the player choose, this method also shows each of the objective to choose from.
     *
     * @param objectives the list of objectives from which the player has to choose one.
     *
     * @see Objective
     */
    void requestPersonalObjectivesChoice(List<Objective> objectives);

    /**
     * Shows the local player's personal objectives.
     * These objectives are secret, and the opponents cannot se them.
     *
     * @param objectives the list of personal objectives of the local player.
     *
     * @see Objective
     */
    void showPersonalObjectives(List<Objective> objectives);

    /**
     * Shows the local player's common objectives.
     * These objective are shared with all the players.
     *
     * @param objectives the list of common objectives of the local player.
     *
     * @see Objective
     */
    void showCommonObjectives(List<Objective> objectives);

    /**
     * Updates the decks.
     * These are intended as the currently drawable cards, not the entire decks.
     *
     * @param drawableCards     the map linking a CardType to its drawable BasicCards.
     * @param numberOfCardsLeft the number of card left in each deck.
     *
     * @see CardType
     * @see BasicCard
     */
    void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft);

    /**
     * Reveals the final game summary of one player, which includes their score for each objective and their final
     * total score.
     *
     * @param nickname          the nickname of the player on which the summary is based.
     * @param objectivePoints   the map linking each objective to its point gained-by value.
     * @param finalScore        the player's score.
     *
     * @see Objective
     */
    void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore);

    /**
     * Reveals the winners of the game, showing their names (and summaries).
     *
     * @param winners the list of winners' nicknames.
     */
    void revealWinners(List<String> winners);

    /**
     * Notifies a remote player disconnection, by showing a message.
     *
     * @param nickname the disconnected player's nickname.
     * @param color    the disconnected player's color.
     * @param quiet    the boolean flagging whether the disconnection must be quiet or not.
     *
     * @see Content
     */
    void notifyRemotePlayerDisconnected(String nickname, Content color, boolean quiet);

    /**
     * Notifies that a player has left the lobby, by showing a message.
     *
     * @param nickname the nickname of whom left the lobby.
     * @param color    the color of whom left the lobby.
     *
     * @see Content
     */
    void notifyPlayerLeftLobby(String nickname, Content color);

    /**
     * Notifies a remote player reconnection, by showing a message.
     * This occurs when server detected that a previously disconnected player has now reestablished a connection.
     *
     * @param nickname the disconnected player's nickname.
     */
    void notifyRemotePlayerReconnected(String nickname);

    /**
     * Notifies that the game has now an aborting timer going on, by showing a message.
     * If enough time passes, the game will automatically be aborted by the server.
     */
    void notifyGameTimeout();

    /**
     * Notifies that a player's turn has been skipped, by showing a message.
     * This occurs when the turn is passed to a currently disconnected player.
     */
    void notifyTurnSkipped();

    /**
     * Shows that a player has no more moves available.
     * This implies that the player is currently stuck in his state.
     *
     * @param nickname the stuck player's nickname.
     */
    void showNoMovesAvailable(String nickname);
}