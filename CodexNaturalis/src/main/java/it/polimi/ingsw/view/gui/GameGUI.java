package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.gui.controllers.GameViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public class GameGUI extends AbstractGUI implements GameView {

    /**
     * Constructor for the class.
     * @param primaryStage
     * @param currentScene
     * @param currentLoader
     * @param controller
     */
    public GameGUI(Stage primaryStage, Scene currentScene, FXMLLoader currentLoader, ClientController controller) {
        this.primaryStage = primaryStage;
        this.currentScene = currentScene;
        this.currentLoader = currentLoader;
        this.controller = controller;
    }

    @Override
    public void notifyLastTurn() {
        currentLoader.<GameViewController>getController().updateStatusLabel("The next turn will be the last.");
    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards) {

    }

    /**
     * Shows a chat message.
     * @param chatMessage the chat message to show.
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        currentLoader.<GameViewController>getController().showChatMessage(chatMessage.getSender(), chatMessage.getMessage());
    }

    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards, List<BasicCard> validCards, List<Corner> validCorners) {

    }

    /**
     * Notifies the user that the turn has changed.
     * @param turnOwner the new turn owner's nickname.
     */
    @Override
    public void turnChanged(String turnOwner) {
        String coloredPlayer = controller.getPlayerColors().get(turnOwner).getTextColorString() +
                turnOwner + Content.EMPTY.getTextColorString();
        currentLoader.<GameViewController>getController().updateStatusLabel(
                controller.getLocalPlayerName().equals(turnOwner) ?
                String.format("It's your turn, %s!", coloredPlayer) :
                String.format("%s is playing their turn...", coloredPlayer));
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
    public void showUserJoined(String nickname, Content color) {
        currentLoader.<MatchLobbyViewController>getController().updatePlayers(nickname, color);
        forceUpdate(); //fix for repaint issues on windows
        if(controller.isGameFull()){
            changeScene("Game.fxml");
            currentLoader.<GameViewController>getController().initializeScene();
        }
    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {

    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {

    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards) {

    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int moveScore) {

    }

    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {

    }

    @Override
    public void showPersonalObjectives(List<Objective> objectives) {

    }

    @Override
    public void showCommonObjectives(List<Objective> objectives) {

    }

    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards) {

    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {

    }

    @Override
    public void revealWinners(List<String> winners) {

    }

    @Override
    public void notifyRemotePlayerDisconnected(String nickname) {
        if(controller.getPlayerColors().get(nickname) != null) {
            String coloredPlayer = controller.getPlayerColors().get(nickname).getTextColorString() +
                    nickname + Content.EMPTY.getTextColorString();
            currentLoader.<GameViewController>getController().updateStatusLabel(String.format(
                    "%s disconnected from the game. We hope they'll be back soon ;)", coloredPlayer));
        }
    }

    @Override
    public void notifyGameTimeout(){
        currentLoader.<GameViewController>getController().updateStatusLabel(String.format(
                "You're the only player left. If no players reconnect in the next %d seconds, you'll win by forfeit.\n",
                GameParameters.getForfeitTime()));
    }

    @Override
    public void notifyGameCanceled(){
        controller.backToSetup();
    }

    @Override
    public void notifyTurnSkipped() {
        currentLoader.<GameViewController>getController().updateStatusLabel("The turn has been skipped because the player isn't connected");
    }

    @Override
    public void showNoMovesAvailable() {
        String turnOwner = controller.getPlayerWithTurn();
        String coloredPlayer = controller.getPlayerColors().get(turnOwner).getTextColorString() +
                turnOwner + Content.EMPTY.getTextColorString();
        currentLoader.<GameViewController>getController().updateStatusLabel(
                turnOwner.equals(controller.getLocalPlayerName()) ?
                String.format("%s, you can no longer make any more moves ;(", coloredPlayer) :
                String.format("%s cannot make any more moves ;)", coloredPlayer)
        );
    }

    @Override
    public void closeView() {

    }
}