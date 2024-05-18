package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.view.SetupView;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.RadioButton;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SetupGUI extends Application implements SetupView {
    private Scene currentScene;

    @Override
    public void updateMatchList(Map<Integer, String> matchList) {

    }

    @Override
    public void newGameSuccess(int gameId) {

    }

    @Override
    public void showCriticalError(String message) {

    }

    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId) {

    }

    @Override
    public void showUserError(String message, int gameId) {

    }

    @Override
    public void showSuccessfulJoin() {

    }

    @Override
    public void start(Stage stage) throws IOException {
        stage.setTitle("Codex Naturalis");
        FXMLLoader loader = new FXMLLoader(SetupGUI.class.getResource("/IPPortMenu.fxml"));
        currentScene = new Scene(loader.load(),1820,980);
        stage.setScene(currentScene);
        stage.show();
    }

    @FXML
    public void connectButtonHandler(){
        String ip = ((Text) currentScene.lookup("ipTextBox")).getText();
        int port = Integer.parseInt(((Text) currentScene.lookup("portTextBox")).getText());
        int protocol = ((RadioButton) currentScene.lookup("tcpRadioButton")).isSelected() ? 1 : 2;
    }
}