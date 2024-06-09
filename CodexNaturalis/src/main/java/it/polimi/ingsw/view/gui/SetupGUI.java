package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.gui.controllers.ConnectionViewController;
import it.polimi.ingsw.view.gui.controllers.MatchBrowserViewController;
import it.polimi.ingsw.view.gui.controllers.MatchLobbyViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class SetupGUI extends AbstractGUI implements SetupView {

    /**
     * Constructor for the class.
     */
    public SetupGUI(Stage stage, Application application) throws IOException {
        this.controller = new ClientController(this, new GraphicalSubmitter());
        URL firaLocation = getClass().getResource(filePath + "fonts/FiraSansCondensed-Regular.ttf");
        URL firaBoldLocation = getClass().getResource(filePath + "fonts/FiraSansCondensed-SemiBold.ttf");
        if(firaLocation != null && firaBoldLocation != null){
            Font.loadFont(firaLocation.toExternalForm(), 14);
            Font.loadFont(firaBoldLocation.toExternalForm(), 14);
        }else{
            System.out.println("Couldn't load the fira font, resorting to fallback system font");
        }
        this.primaryStage = stage;
        stage.setTitle("Codex Naturalis");
        stage.setMinWidth(1280);
        stage.setMinHeight(720);
        stage.setOnCloseRequest((WindowEvent event) -> System.exit(0));
        currentLoader = new FXMLLoader(getClass().getResource(filePath + "Connection.fxml"));
        currentScene = new Scene(currentLoader.load(),1820,980);
        currentScene.setOnKeyReleased((e) -> {
            if(e.getCode() == KeyCode.F12){
                stage.setFullScreen(!stage.isFullScreen());
            }
        });
        currentLoader.<ConnectionViewController>getController().setController(controller);
        currentLoader.<ConnectionViewController>getController().setApplication(application);
        stage.setScene(currentScene);
        stage.getIcons().add(new Image(filePath + "images/CodexNaturalisColoredLogo.png"));
        stage.show();
    }

    /**
     * Updates the match list browser.
     * @param matchList the match list.
     */
    @Override
    public void updateMatchList(List<GameInfo> matchList){
        changeScene("MatchBrowser.fxml");
        currentLoader.<MatchBrowserViewController>getController().addMatches(matchList);
    }

    /**
     * Requests the newly created game's available colors.
     * @param gameId the ID of the newly created game.
     */
    @Override
    public void newGameSuccess(int gameId){
        controller.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Shows the critical error pop-up.
     * @param message the error message.
     */
    @Override
    public void showCriticalError(String message){
        currentLoader.<MatchBrowserViewController>getController().showCriticalError(message);
    }

    /**
     * Shows the game join pop-up.
     * @param colors the list of available colors.
     * @param gameId the game's id.
     */
    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId){
        currentLoader.<MatchBrowserViewController>getController().showJoinGameDialog(colors, gameId);
    }

    /**
     * Shows the user error pop-up.
     * @param message the error message.
     */
    @Override
    public void showUserError(String message, int gameId){
        currentLoader.<MatchBrowserViewController>getController().showUserError(message);
    }

    /**
     * Changes the scene, notifying the user that they successfully joined the lobby.
     */
    @Override
    public void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers) {
        changeScene("MatchLobby.fxml");
        currentLoader.<MatchLobbyViewController>getController().initializeLabel(nickname, color, numberOfPlayers);
        GameGUI gameGUI = new GameGUI(primaryStage, currentScene, currentLoader, controller);
        controller.setGameView(gameGUI);
    }
}