package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.view.SetupView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SetupGUI extends Application implements SetupView {

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
        FXMLLoader loader = new FXMLLoader(SetupGUI.class.getResource("/JFXTest.fxml"));
        Scene scene = new Scene(loader.load(),1820,980);
        stage.setScene(scene);
        stage.show();
    }
}