package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.view.gui.controllers.ViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;

import java.io.IOException;

/**
 * Lets an easier way of managing scenes.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class SceneManager {

    //path where the scenes are found.
    private final String filePath = "/scenes/";

    //the current scene.
    private final Scene scene;

    //used to load the scenes and get the current scene's controller.
    private FXMLLoader currentLoader;

    /**
     * Constructor of the class.
     *
     * @param sceneName     the name of the first scene to load.
     *
     * @throws IOException  when the scene loading fails.
     */
    public SceneManager(String sceneName) throws IOException{
        this.currentLoader = new FXMLLoader(getClass().getResource(filePath + sceneName));
        this.scene = new Scene(currentLoader.load(),1400,800);
    }

    /**
     * Gets the current scene controller instance.
     *
     * @param <T>   class type of the expected controller.
     *
     * @return      the current scene controller.
     *
     * @see ViewController
     */
    public <T extends ViewController> T getController(){
        return currentLoader.getController();
    }

    /**
     * Changes (and loads) the current scene.
     *
     * @param file   the FXML resource path.
     * @param client the client associated to this machine.
     *
     * @see Client
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

    /**
     * Gets the current scene.
     *
     * @return the currently loaded scene.
     */
    public Scene getScene() {
        return scene;
    }
}