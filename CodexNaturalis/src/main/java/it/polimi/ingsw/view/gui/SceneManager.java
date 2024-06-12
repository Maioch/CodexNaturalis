package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.view.gui.controllers.ViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

public class SceneManager {
    private final String filePath = "/scenes/";
    private final Scene scene;
    private FXMLLoader currentLoader;

    public SceneManager(String sceneName) throws IOException{
        this.currentLoader = new FXMLLoader(getClass().getResource(filePath + sceneName));
        this.scene = new Scene(currentLoader.load(),1820,980);
    }

    public <T extends ViewController> T getController(){
        return currentLoader.getController();
    }

    /**
     * Changes (and loads) the current scene.
     *
     * @param file the FXML resource path.
     */
    public void changeScene(String file, Client client){
        currentLoader = new FXMLLoader(getClass().getResource(filePath + file));
        try {
            scene.setRoot(currentLoader.load());
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        getController().setClient(client);
    }

    public Scene getScene() {
        return scene;
    }
}