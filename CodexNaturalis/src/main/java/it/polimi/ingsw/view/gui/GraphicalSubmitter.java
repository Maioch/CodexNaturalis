package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.view.EventSubmitter;
import javafx.application.Platform;

public class GraphicalSubmitter implements EventSubmitter {
    @Override
    public void submit(Runnable action) {
        Platform.runLater(action);
    }
}