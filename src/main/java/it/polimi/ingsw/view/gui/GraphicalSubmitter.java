package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.view.EventSubmitter;
import javafx.application.Platform;

/**
 * Submits events for the GUI.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class GraphicalSubmitter implements EventSubmitter {

    /**
     * Class constructor.
     */
    public GraphicalSubmitter() {}

    /**
     * Submits a new action.
     *
     * @param action the action to submit.
     */
    @Override
    public void submit(Runnable action) {
        Platform.runLater(action);
    }
}