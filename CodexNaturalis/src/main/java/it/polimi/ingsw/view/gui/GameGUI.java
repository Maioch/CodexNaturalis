package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.gui.controllers.GameViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;

import java.util.List;
import java.util.Map;

/**
 * The GUI associated to the gameplay phase.
 */
public class GameGUI implements GameView {
    public enum ToastType{
        TIMEOUT, PLACE, DRAW
    }

    private final SceneManager sceneManager;
    private final Client client;

    /**
     * Constructor for the class.
     *
     * @param sceneManager the loader of the current scene. Used to get the current scene view client.getController().
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
     * @param drawableCards the list of drawable cards, for each type.
     * @param numberOfCardsLeft the number of cards left, for each type.
     */
    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        sceneManager.<GameViewController>getController().drawCard(drawableCards, numberOfCardsLeft);
        sceneManager.<GameViewController>getController().updateStatusLabel("Draw a card", ToastType.DRAW.toString());
    }

    /**
     * Shows a chat message.
     * @param chatMessage the chat message to show.
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        sceneManager.<GameViewController>getController().showChatMessage(chatMessage.getSender(), chatMessage.getMessage());
    }

    /**
     * Requests the player to place a card.
     *
     * @param handCards the player's hand cards.
     * @param placedCards the player's board.
     */
    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards) {
        sceneManager.<GameViewController>getController().placeCard(handCards, placedCards);
        sceneManager.<GameViewController>getController().updateStatusLabel("Place a card, codex wizard!", ToastType.PLACE.toString());
    }

    /**
     * Notifies the user that the turn has changed.
     * @param turnOwner the new turn owner's nickname.
     */
    @Override
    public void turnChanged(String turnOwner) {
        sceneManager.<GameViewController>getController().updateStatusLabel(
                client.getController().getLocalPlayerName().equals(turnOwner) ?
                String.format("It's your turn, %s!", turnOwner) :
                String.format("%s is playing their turn...", turnOwner));
        sceneManager.<GameViewController>getController().setCurrentTurnOwner(turnOwner);
    }

    /**
     * Shows an error message.
     * @param message the error to show.
     */
    @Override
    public void showErrorMessage(String message) {
        sceneManager.<GameViewController>getController().updateStatusLabel(message);
    }

    /**
     * Shows that a user has joined the game.
     * @param nickname the joined user nickname.
     * @param color the joined user color.
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
     *
     * @param nickname the remote player's nickname.
     * @param handCards the list of remote player's hand cards.
     */
    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {
        sceneManager.<GameViewController>getController().updateRemotePlayerCards(nickname, handCards);
    }

    /**
     * Updates the local player's hand.
     *
     * @param handCards the list of local player's hand cards.
     */
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {
        sceneManager.<GameViewController>getController().updateLocalPlayerCards(handCards);
    }

    /**
     * Requests to the player the starter card side to place.
     *
     * @param playerCards the player's hand cards.
     */
    @Override
    public void requestStarterSide(List<CardSides> playerCards) {
        sceneManager.<GameViewController>getController().updateLocalPlayerCards(playerCards.subList(1, playerCards.size()));
        sceneManager.<GameViewController>getController().chooseStarterSide(playerCards.getFirst());
    }

    /**
     * Updates a certain player's board.
     *
     * @param nickname the player's nickname.
     * @param placedCards the player's board.
     * @param score the new move score.
     */
    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int score) {
        if(client.getController().getLocalPlayerName().equals(nickname)) {
            sceneManager.<GameViewController>getController().updateLocalPlayerBoard(placedCards);
        } else {
            sceneManager.<GameViewController>getController().updateRemotePlayerBoard(nickname, placedCards);
        }
        sceneManager.<GameViewController>getController().updateScore(nickname, score);
    }

    /**
     * Requests the personal objective choice.
     *
     * @param objectives the objective options.
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
     */
    @Override
    public void showPersonalObjectives(List<Objective> objectives) {
        sceneManager.<GameViewController>getController().setPersonalObjectives(objectives.getFirst());
    }

    /**
     * Shows the common objectives.
     *
     * @param objectives the list of common objectives.
     */
    @Override
    public void showCommonObjectives(List<Objective> objectives) {
        sceneManager.<GameViewController>getController().setCommonObjectives(
                objectives.getFirst(), objectives.getLast());
    }

    /**
     * Updates the decks' view.
     *
     * @param drawableCards the drawable cards of the deck.
     * @param numberOfCardsLeft the deck's left cards.
     */
    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType,Integer> numberOfCardsLeft) {
        sceneManager.<GameViewController>getController().updateDecks(drawableCards, numberOfCardsLeft);
    }

    /**
     * Reveals the final summary of a player.
     *
     * @param nickname the player's nickname.
     * @param objectivePoints the map linking the player's objectives to the points each made.
     * @param finalScore the player's final score.
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
        sceneManager.<GameViewController>getController().revealWinners(winners);
    }

    /**
     * Notifies that a remote player has disconnected.
     *
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color) {
        sceneManager.<GameViewController>getController().updateStatusLabel(String.format(
                "%s disconnected from the game", nickname));
    }

    /**
     * Notifies that a player has left the lobby.
     *
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    @Override
    public void notifyPlayerLeftLobby(String nickname, Content color) {
        sceneManager.<MatchLobbyViewController>getController().removePlayer(nickname);
    }

    /**
     * Notifies that a remoted player has reconnected.
     *
     * @param nickname the player's nickname.
     */
    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        sceneManager.<GameViewController>getController().updateStatusLabel(String.format(
                "%s reconnected to the game", nickname));
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
                GameParameters.getForfeitTime()), ToastType.TIMEOUT.toString());
        sceneManager.<GameViewController>getController().setChatDisable(true);
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
     * Notifies the player that he cannot do any move.
     */
    @Override
    public void showNoMovesAvailable() {
        String turnOwner = client.getController().getPlayerWithTurn();
        sceneManager.<GameViewController>getController().updateStatusLabel(
                turnOwner.equals(client.getController().getLocalPlayerName()) ?
                String.format("%s, you can no longer make any more moves", turnOwner) :
                String.format("%s cannot make any more moves", turnOwner)
        );
    }

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