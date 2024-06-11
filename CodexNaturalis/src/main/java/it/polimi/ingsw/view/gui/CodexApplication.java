package it.polimi.ingsw.view.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Class representing the JavaFX application (which starts the scene viewing).
 */
public class CodexApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        new SetupGUI().showInterface(stage, this);
    }
}