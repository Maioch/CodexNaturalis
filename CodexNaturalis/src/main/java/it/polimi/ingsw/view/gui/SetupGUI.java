package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.gui.controllers.ConnectionViewController;
import it.polimi.ingsw.view.gui.controllers.MatchBrowserViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class SetupGUI extends Application implements SetupView {
    private Stage primaryStage;
    private Scene currentScene;
    private final ClientController controller;

    public SetupGUI(){
        this.controller = new ClientController(this, new GraphicalSubmitter());
    }

    @Override
    public void updateMatchList(Map<Integer, String> matchList){
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/MatchBrowser.fxml"));
        try {
            currentScene = new Scene(loader.load(), currentScene.getWidth(), currentScene.getHeight());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        MatchBrowserViewController viewController = loader.getController();
        viewController.addMatches(matchList);
        primaryStage.setScene(currentScene);
    }

    @Override
    public void newGameSuccess(int gameId){

    }

    @Override
    public void showCriticalError(String message){

    }

    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId){

    }

    @Override
    public void showUserError(String message, int gameId){

    }

    @Override
    public void showSuccessfulJoin(){

    }

    @Override
    public void start(Stage stage) throws IOException{
        URL firaLocation = getClass().getResource("/scenes/fonts/FiraSansCondensed-Regular.ttf");
        URL firaBoldLocation = getClass().getResource("/scenes/fonts/FiraSansCondensed-SemiBold.ttf");
        if(firaLocation != null && firaBoldLocation != null){
            Font.loadFont(firaLocation.toExternalForm(), 14);
            Font.loadFont(firaBoldLocation.toExternalForm(), 14);
        }else{
            System.out.println("Couldn't load the fira font, resorting to fallback system font");
        }
        this.primaryStage = stage;
        stage.setTitle("Codex Naturalis");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/scenes/Connection.fxml"));
        currentScene = new Scene(loader.load(),1820,980);
        loader.<ConnectionViewController>getController().setClientController(controller);
        loader.<ConnectionViewController>getController().setApplication(this);
        stage.setScene(currentScene);
        stage.getIcons().add(new Image("/scenes/images/CodexNaturalisColoredLogo.png"));
        stage.show();
    }
}