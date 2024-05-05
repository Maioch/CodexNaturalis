package it.polimi.ingsw.view;
import javafx.application.Platform;

public class GraphicalSubmitter implements EventSubmitter{
    @Override
    public void submit(Runnable action) {
        Platform.runLater(action);
    }
}