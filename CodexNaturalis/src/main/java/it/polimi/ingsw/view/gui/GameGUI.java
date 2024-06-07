package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.gui.controllers.GameViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameGUI extends AbstractGUI implements GameView {

    public enum ToastType{
        TIMEOUT, PLACE, DRAW
    }

    /**
     * Constructor for the class.
     *
     * @param primaryStage the main application stage.
     * @param currentScene the scene currently displayed.
     * @param currentLoader the loader of the current scene. Used to get the current scene view controller.
     * @param controller the client controller.
     */
    public GameGUI(Stage primaryStage, Scene currentScene, FXMLLoader currentLoader, ClientController controller) {
        this.primaryStage = primaryStage;
        this.currentScene = currentScene;
        this.currentLoader = currentLoader;
        this.controller = controller;
    }

    /**
     * Notifies that the next turn will be the last.
     */
    @Override
    public void notifyLastTurn() {
        currentLoader.<GameViewController>getController().updateStatusLabel("The next turn will be the last");
    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {

    }

    /**
     * Shows a chat message.
     * @param chatMessage the chat message to show.
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        currentLoader.<GameViewController>getController().showChatMessage(chatMessage.getSender(), chatMessage.getMessage());
    }

    /**
     * Requests the player to place a card.
     *
     * @param handCards the player's hand cards.
     * @param placedCards the player's board.
     */
    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards) {
        currentLoader.<GameViewController>getController().placeCard(handCards, placedCards);
        currentLoader.<GameViewController>getController().updateStatusLabel("Place a card, codex wizard!", ToastType.PLACE.toString());
    }

    /**
     * Notifies the user that the turn has changed.
     * @param turnOwner the new turn owner's nickname.
     */
    @Override
    public void turnChanged(String turnOwner) {
        GameViewController gameViewController = currentLoader.getController();
        gameViewController.updateStatusLabel(
                controller.getLocalPlayerName().equals(turnOwner) ?
                String.format("It's your turn, %s!", turnOwner) :
                String.format("%s is playing their turn...", turnOwner));
        gameViewController.setCurrentTurnOwner(turnOwner);
    }

    /**
     * Shows an error message.
     * @param message the error to show.
     */
    @Override
    public void showErrorMessage(String message) {
        currentLoader.<GameViewController>getController().updateStatusLabel(message);
    }

    /**
     * Shows that a user has joined the game.
     * @param nickname the joined user nickname.
     * @param color the joined user color.
     */
    @Override
    public void showUserJoined(String nickname, Content color, boolean isGameFull) {
        currentLoader.<MatchLobbyViewController>getController().updatePlayers(nickname, color);
        //forceUpdate(); //fix for repaint issues on windows
        if(isGameFull){
            changeScene("Game.fxml");
            currentLoader.<GameViewController>getController().initializeScene();
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
        currentLoader.<GameViewController>getController().updateRemotePlayerCards(nickname, handCards);
    }

    /**
     * Updates the local player's hand.
     *
     * @param handCards the list of local player's hand cards.
     */
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {
        currentLoader.<GameViewController>getController().updateLocalPlayerCards(handCards);
    }

    /**
     * Requests to the player the starter card side to place.
     *
     * @param playerCards the player's hand cards.
     */
    @Override
    public void requestStarterSide(List<CardSides> playerCards) {
        /*GraphicalSubmitter graphicalSubmitter = new GraphicalSubmitter();
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        for (int i = 0; i < 50; i++) {
            int j = 1;
            for (String nickname : controller.getRemotePlayerNames()) {
                int finalI = i;
                executorService.schedule(() -> graphicalSubmitter.submit(() ->
                        currentLoader.<GameViewController>getController().updateScore(nickname, finalI)
                ), i * j, TimeUnit.SECONDS);
                j++;
            }
        }*/
        currentLoader.<GameViewController>getController().updateLocalPlayerCards(playerCards.subList(1, playerCards.size()));
        currentLoader.<GameViewController>getController().chooseStarterSide(playerCards.getFirst());
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
        if(controller.getLocalPlayerName().equals(nickname)) {
            currentLoader.<GameViewController>getController().updateLocalPlayerBoard(placedCards);
        } else {
            currentLoader.<GameViewController>getController().updateRemotePlayerBoard(nickname, placedCards);
        }
        currentLoader.<GameViewController>getController().updateScore(nickname, score);
    }

    /**
     * Requests the personal objective choice.
     *
     * @param objectives the objective options.
     */
    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        currentLoader.<GameViewController>getController().choosePersonalObjective(
                objectives.getFirst(), objectives.getLast());
    }

    /**
     * Shows the local player's personal objectives.
     *
     * @param objectives the list of personal objectives.
     */
    @Override
    public void showPersonalObjectives(List<Objective> objectives) {
        currentLoader.<GameViewController>getController().setPersonalObjectives(objectives.getFirst());
    }

    /**
     * Shows the common objectives.
     *
     * @param objectives the list of common objectives.
     */
    @Override
    public void showCommonObjectives(List<Objective> objectives) {
        currentLoader.<GameViewController>getController().setCommonObjectives(
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
        currentLoader.<GameViewController>getController().updateDecks(drawableCards, numberOfCardsLeft);
    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {

    }

    @Override
    public void revealWinners(List<String> winners) {

    }

    /**
     * Notifies that a remote player has disconnected.
     *
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color) {
        currentLoader.<GameViewController>getController().updateStatusLabel(String.format(
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
        currentLoader.<MatchLobbyViewController>getController().removePlayer(nickname);
    }

    /**
     * Notifies that a remoted player has reconnected.
     *
     * @param nickname the player's nickname.
     */
    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        currentLoader.<GameViewController>getController().updateStatusLabel(String.format(
                "%s reconnected to the game", nickname));
        currentLoader.<GameViewController>getController().hideStatusLabel(ToastType.TIMEOUT.toString());
        currentLoader.<GameViewController>getController().setChatDisable(false);
    }

    /**
     * Notifies that the game has started the terminating timeout.
     */
    @Override
    public void notifyGameTimeout(){
        currentLoader.<GameViewController>getController().updateStatusLabel(String.format(
                "If no players reconnect in the next %d seconds, you'll win",
                GameParameters.getForfeitTime()), ToastType.TIMEOUT.toString());
        currentLoader.<GameViewController>getController().setChatDisable(true);
    }

    /**
     * Notifies that the game has canceled.
     */
    @Override
    public void notifyGameCanceled(){
        controller.backToSetup();
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Notifies that the expected turn has been skipped.
     */
    @Override
    public void notifyTurnSkipped() {
        currentLoader.<GameViewController>getController().updateStatusLabel(
                "The turn has been skipped because the player isn't connected");
    }

    /**
     * Notifies the player that he cannot do any move.
     */
    @Override
    public void showNoMovesAvailable() {
        String turnOwner = controller.getPlayerWithTurn();
        currentLoader.<GameViewController>getController().updateStatusLabel(
                turnOwner.equals(controller.getLocalPlayerName()) ?
                String.format("%s, you can no longer make any more moves", turnOwner) :
                String.format("%s cannot make any more moves", turnOwner)
        );
    }

    @Override
    public void closeView() {

    }
}