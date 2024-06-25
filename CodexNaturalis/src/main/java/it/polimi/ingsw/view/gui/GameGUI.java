package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.ChatMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.gui.controllers.GameViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;

import java.util.List;
import java.util.Map;

/**
 * The GUI associated to the gameplay phase.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class GameGUI implements GameView {

    /**
     * Represents the three types of permanent toast messages.
     */
    public enum ToastType{
        /**
         * Toast message shown whenever the player is left alone and the game's timeout period starts.
         */
        TIMEOUT,

        /**
         * Toast message shown whenever the player has to place a card.
         */
        PLACE,

        /**
         * Toast message shown whenever the player has to draw a card.
         */
        DRAW
    }

    //manages scene changes and obtains the current scene's controller.
    private final SceneManager sceneManager;

    //the client instance used for the entire program's lifecycle
    private final Client client;

    /**
     * Constructor for the class.
     *
     * @param sceneManager the loader of the current scene. Used to get the current scene view client.getController().
     * @param client the Client instance.
     *
     * @see SceneManager
     * @see Client
     */
    public GameGUI(SceneManager sceneManager, Client client) {
        this.sceneManager = sceneManager;
        this.client = client;
    }

    /**
     * Notifies that the next turn will be the last.
     */
    @Override
    public void notifyLastTurn() {
        sceneManager.<GameViewController>getController().updateStatusLabel("The next turn will be the last");
    }

    /**
     * Request to the player a card drawing choice.
     *
     * @param drawableCards     the list of drawable cards, for each type.
     * @param numberOfCardsLeft the number of cards left, for each type.
     *
     * @see CardType
     * @see BasicCard
     */
    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        sceneManager.<GameViewController>getController().drawCard(drawableCards, numberOfCardsLeft);
        sceneManager.<GameViewController>getController().updateStatusLabel("Draw a card", ToastType.DRAW.toString());
    }

    /**
     * Shows a chat message.
     * Particularly, it adds it in the chat scroll pane.
     *
     * @param chatMessage the chat message to show.
     *
     * @see ChatMessage
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        sceneManager.<GameViewController>getController().showChatMessage(
                chatMessage.getSender(),
                chatMessage.getRecipients(),
                chatMessage.getMessage());
    }

    /**
     * Requests the player to place a card.
     *
     * @param handCards     the player's hand cards.
     * @param placedCards   the player's board.
     *
     * @see CardSides
     * @see BasicCard
     */
    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards) {
        sceneManager.<GameViewController>getController().placeCard(handCards, placedCards);
        sceneManager.<GameViewController>getController().updateStatusLabel("Place a card", ToastType.PLACE.toString());
    }

    /**
     * Notifies the user that the turn has changed.
     *
     * @param turnOwner the new turn owner's nickname.
     */
    @Override
    public void turnChanged(String turnOwner) {
        if(!client.getController().getLocalPlayerName().equals(turnOwner)){
            sceneManager.<GameViewController>getController().updateStatusLabel(String.format("%s is playing their turn...", turnOwner));
        }
        sceneManager.<GameViewController>getController().setCurrentTurnOwner(turnOwner);
        sceneManager.<GameViewController>getController().updateLocalPlayerCards(client.getController().getLocalPlayerHand());
    }

    /**
     * Shows an error message.
     *
     * @param message the error to show.
     */
    @Override
    public void showErrorMessage(String message) {
        sceneManager.<GameViewController>getController().updateStatusLabel(message);
    }

    /**
     * Shows that a user has joined the game.
     *
     * @param nickname      the joined user nickname.
     * @param color         the joined user color.
     * @param isGameFull    the boolean flagging whether the game is full or not.
     *
     * @see Content
     */
    @Override
    public void showUserJoined(String nickname, Content color, boolean isGameFull) {
        sceneManager.<MatchLobbyViewController>getController().updatePlayers(nickname, color);
        if(isGameFull){
            sceneManager.changeScene("Game.fxml", client);
            sceneManager.<GameViewController>getController().initializeScene();
        }
    }

    /**
     * Updates a remote player's hand.
     * The local player must selectively observe an enemy in order to see the specified player's (back) hand cards.
     *
     * @param nickname the remote player's nickname.
     * @param handCards the list of remote player's hand cards.
     *
     * @see BasicCard
     */
    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {
        sceneManager.<GameViewController>getController().updateRemotePlayerCards(nickname, handCards);
    }

    /**
     * Updates the local player's hand.
     *
     * @param handCards the list of local player's hand cards.
     *
     * @see CardSides
     */
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {
        sceneManager.<GameViewController>getController().updateLocalPlayerCards(handCards);
    }

    /**
     * Requests to the player the starter card side to place.
     *
     * @param playerCards the player's hand cards.
     *
     * @see CardSides
     */
    @Override
    public void requestStarterSide(List<CardSides> playerCards) {
        sceneManager.<GameViewController>getController().updateLocalPlayerCards(playerCards.subList(1, playerCards.size()));
        sceneManager.<GameViewController>getController().chooseStarterSide(playerCards.getFirst());
    }

    /**
     * Updates a certain player's board.
     * The local player must selectively observe an enemy in order to see his board.
     *
     * @param nickname      the player's nickname.
     * @param placedCards   the player's board.
     * @param score         the new move score.
     *
     * @see BasicCard
     */
    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int score) {
        if(client.getController().getLocalPlayerName().equals(nickname)) {
            sceneManager.<GameViewController>getController().hideStatusLabel(GameGUI.ToastType.PLACE.toString());
            sceneManager.<GameViewController>getController().updateLocalPlayerBoard(placedCards);
            sceneManager.<GameViewController>getController().enableViewSwitching();
        } else {
            sceneManager.<GameViewController>getController().updateRemotePlayerBoard(nickname, placedCards);
        }
        sceneManager.<GameViewController>getController().updateScore(nickname, score);
    }

    /**
     * Requests the personal objective choice.
     *
     * @param objectives the objective options.
     *
     * @see Objective
     */
    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        sceneManager.<GameViewController>getController().choosePersonalObjective(
                objectives.getFirst(), objectives.getLast());
    }

    /**
     * Shows the local player's personal objectives.
     *
     * @param objectives the list of personal objectives.
     *
     * @see Objective
     */
    @Override
    public void showPersonalObjectives(List<Objective> objectives) {
        sceneManager.<GameViewController>getController().setPersonalObjectives(objectives.getFirst());
    }

    /**
     * Shows the common objectives.
     *
     * @param objectives the list of common objectives.
     *
     * @see Objective
     */
    @Override
    public void showCommonObjectives(List<Objective> objectives) {
        sceneManager.<GameViewController>getController().setCommonObjectives(
                objectives.getFirst(), objectives.getLast());
    }

    /**
     * Updates the decks' view.
     *
     * @param drawableCards     the drawable cards of the deck.
     * @param numberOfCardsLeft the deck's left cards.
     *
     * @see CardType
     * @see BasicCard
     */
    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        sceneManager.<GameViewController>getController().updateDecks(drawableCards, numberOfCardsLeft);
    }

    /**
     * Reveals the final summary of a player.
     *
     * @param nickname          the player's nickname.
     * @param objectivePoints   the map linking the player's objectives to the points each made.
     * @param finalScore        the player's final score.
     *
     * @see Objective
     */
    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {
        sceneManager.<GameViewController>getController().addPlayerScoreToSummary(nickname, objectivePoints, finalScore);
    }

    /**
     * Shows the winners of the game.
     *
     * @param winners the list of winners' nicknames.
     */
    @Override
    public void revealWinners(List<String> winners) {
        sceneManager.<GameViewController>getController().setChatDisable(true);
        sceneManager.<GameViewController>getController().revealWinners(winners);
    }

    /**
     * Notifies that a remote player has disconnected.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     * @param quiet    flag that determines whether to update the status label (true for update).
     *
     * @see Content
     */
    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color, boolean quiet) {
        if(!quiet) {
            sceneManager.<GameViewController>getController().updateStatusLabel(String.format(
                    "%s disconnected from the game", nickname));
        }
        sceneManager.<GameViewController>getController().setPlayerStatus(nickname,false);
    }

    /**
     * Notifies that a player has left the lobby.
     *
     * @param nickname  the player's nickname.
     * @param color     the player's color.
     *
     * @see Content
     */
    @Override
    public void notifyPlayerLeftLobby(String nickname, Content color) {
        sceneManager.<MatchLobbyViewController>getController().removePlayer(nickname);
    }

    /**
     * Notifies that a remote player has reconnected.
     *
     * @param nickname the player's nickname.
     */
    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        sceneManager.<GameViewController>getController().updateStatusLabel(String.format(
                "%s reconnected to the game", nickname));
        sceneManager.<GameViewController>getController().setPlayerStatus(nickname,true);
        sceneManager.<GameViewController>getController().hideStatusLabel(ToastType.TIMEOUT.toString());
        sceneManager.<GameViewController>getController().setChatDisable(false);
    }

    /**
     * Notifies that the game has started the terminating timeout.
     */
    @Override
    public void notifyGameTimeout(){
        sceneManager.<GameViewController>getController().updateStatusLabel(String.format(
                "If no players reconnect in the next %d seconds, you'll win",
                Parameters.getForfeitTime()), ToastType.TIMEOUT.toString());
        sceneManager.<GameViewController>getController().setChatDisable(true);
        sceneManager.<GameViewController>getController().disableCardHand();
    }

    /**
     * Notifies that the game has canceled.
     */
    @Override
    public void notifyGameCanceled(){
        client.getController().backToSetup();
        client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Notifies that the expected turn has been skipped.
     */
    @Override
    public void notifyTurnSkipped() {
        sceneManager.<GameViewController>getController().updateStatusLabel(
                "The turn has been skipped because the player isn't connected");
    }

    /**
     * Notifies the player specified that he cannot do any moves.
     *
     * @param nickname the player that cannot do any moves.
     */
    @Override
    public void showNoMovesAvailable(String nickname) {
        sceneManager.<GameViewController>getController().updateStatusLabel(
                nickname.equals(client.getController().getLocalPlayerName()) ?
                String.format("%s, you can no longer make any more moves", nickname) :
                String.format("%s cannot make any more moves", nickname)
        );
    }

    /**
     * Shows that the local player has disconnected.
     * This occurs when the client no more receives ack pings from the server.
     */
    @Override
    public void showDisconnectionMessage(){
        client.getController().stop();
        String playerName = client.getController().getLocalPlayerName();
        int gameId = client.getController().getGameId();
        client.createController();
        sceneManager.getController().handleDisconnection(new JoinGameMessage(
                Status.RECONNECT, playerName, null, null, gameId));
    }
}