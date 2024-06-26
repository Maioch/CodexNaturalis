package it.polimi.ingsw.view.gui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Represents the JavaFX application (which starts the scene viewing).
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CodexApplication extends Application {

    /**
     * Class constructor.
     */
    public CodexApplication() {
        super();
    }

    /**
     * Starts the GUI running.
     *
     * @param stage the JavaFX stage instance.
     *
     * @throws IOException when the scene isn't loaded correctly.
     */
    @Override
    public void start(Stage stage) throws IOException {
        new SetupGUI().showInterface(stage, this);
    }
}