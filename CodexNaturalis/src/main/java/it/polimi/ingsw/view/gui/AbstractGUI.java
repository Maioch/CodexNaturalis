package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.view.gui.controllers.ViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public abstract class AbstractGUI {
    protected final String filePath = "/scenes/";
    protected Stage primaryStage;
    protected Scene currentScene;
    protected FXMLLoader currentLoader;
    protected ClientController controller;

    /**
     * Changes (and loads) the current scene.
     * @param file the FXML resource path.
     */
    protected void changeScene(String file){
        currentLoader = new FXMLLoader(getClass().getResource(filePath + file));
        try {
            currentScene.setRoot(currentLoader.load());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        currentLoader.<ViewController>getController().setController(controller);
    }
}