package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.gui.controllers.ConnectionViewController;
import it.polimi.ingsw.view.gui.controllers.MatchBrowserViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Used when the client chooses to play the GUI version of the game; this class represents the GUI for the player
 * reception phase, before entering an actual game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class SetupGUI implements SetupView {

    //manages scene changes and obtains the current scene's controller.
    private SceneManager sceneManager;

    //the client instance used for the entire program's lifecycle
    private final Client client;

    /**
     * Constructor for the class.
     */
    public SetupGUI(){
        this.client = new Client(new GraphicalSubmitter(), this);
        client.createController();
    }

    /**
     * Shows the graphical user interface.
     *
     * @param stage         the JavaFX stage of the GUI.
     * @param application   the JavaFX application of the running GUI.
     *
     * @throws IOException  if the scene isn't loaded correctly.
     */
    public void showInterface(Stage stage, Application application) throws IOException{
        URL firaLocation = getClass().getResource("/scenes/fonts/FiraSansCondensed-Regular.ttf");
        URL firaBoldLocation = getClass().getResource("/scenes/fonts/FiraSansCondensed-SemiBold.ttf");
        if(firaLocation != null && firaBoldLocation != null){
            Font.loadFont(firaLocation.toExternalForm(), 14);
            Font.loadFont(firaBoldLocation.toExternalForm(), 14);
        }else{
            System.out.println("Couldn't load the fira font, resorting to fallback system font");
        }
        stage.setTitle("Codex Naturalis");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setOnCloseRequest((WindowEvent event) -> System.exit(0));
        sceneManager = new SceneManager("Connection.fxml");
        sceneManager.getScene().setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.F12){
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
        sceneManager.getController().setClient(client);
        sceneManager.<ConnectionViewController>getController().setApplication(application);
        stage.setScene(sceneManager.getScene());
        stage.getIcons().add(new Image("/scenes/images/interface/codexNaturalisColoredLogo.png"));
        stage.show();
    }

    /**
     * Updates the match list browser.
     *
     * @param matchList the match list.
     *
     * @see GameInfo
     */
    @Override
    public void updateMatchList(List<GameInfo> matchList){
        if(client.getConnectionSettings() == null) {
            client.setConnectionSettings(sceneManager.<ConnectionViewController>getController().getConnectionSettings());
        }
        sceneManager.changeScene("MatchBrowser.fxml", client);
        sceneManager.<MatchBrowserViewController>getController().addMatches(matchList);
    }

    /**
     * Requests the newly created game's available colors.
     *
     * @param gameId the ID of the newly created game.
     */
    @Override
    public void newGameSuccess(int gameId){
        client.getController().sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Shows the critical error pop-up.
     *
     * @param message the error message.
     */
    @Override
    public void showCriticalError(String message){
        sceneManager.<MatchBrowserViewController>getController().showCriticalError(message);
    }

    /**
     * Shows the game join pop-up.
     *
     * @param colors the list of available colors.
     * @param gameId the game's id.
     *
     * @see Content
     */
    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId){
        sceneManager.<MatchBrowserViewController>getController().showJoinGameDialog(colors, gameId);
    }

    /**
     * Shows the user error pop-up.
     *
     * @param message   the error message.
     * @param gameId    the ID of the game the client is joining ()NOT USED IN THIS IMPLEMENTATION).
     */
    @Override
    public void showUserError(String message, int gameId){
        sceneManager.<MatchBrowserViewController>getController().showUserError(message);
    }

    /**
     * Changes the scene, notifying the user that they successfully joined the lobby.
     *
     * @param nickname          the local player's nickname.
     * @param color             the local player's color.
     * @param numberOfPlayers   the number of players the game will be made of.
     */
    @Override
    public void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers) {
        sceneManager.changeScene("MatchLobby.fxml", client);
        sceneManager.<MatchLobbyViewController>getController().initializeLabel(nickname, color, numberOfPlayers);
        GameGUI gameGUI = new GameGUI(sceneManager, client);
        client.getController().setGameView(gameGUI);
    }

    /**
     * Shows that the user has unexpectedly disconnected.
     */
    @Override
    public void showDisconnectionMessage(){
        client.getController().stop();
        client.createController();
        sceneManager.getController().handleDisconnection(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Shows a reconnection error pop up.
     *
     * @param message the error message to show.
     */
    @Override
    public void showReconnectionError(String message) {
        sceneManager.getController().showReconnectionError(message);
    }
}