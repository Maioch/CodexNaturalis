package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.gui.controllers.LoginViewController;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SetupGUI extends Application implements SetupView {
    private Scene currentScene;
    private final ClientController controller;

    public SetupGUI(){
        this.controller = new ClientController(this, new GraphicalSubmitter());
    }

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
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/IPPortMenu.fxml"));
        currentScene = new Scene(loader.load(),1820,980);
        loader.<LoginViewController>getController().setClientController(controller);
        stage.setScene(currentScene);
        stage.getIcons().add(new Image("/scenes/images/CodexNaturalisColoredLogo.png"));
        stage.show();
    }
}