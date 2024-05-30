package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.view.gui.controllers.ViewController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

    /**
     * Record class representing a grid entry (with its constraint value, and the node in it)
     * @param constraint the integer value of the constraint (percentage)
     * @param node the entry node
     */
    protected record GridEntry(int constraint, Node node){}

    /**
     * Adds columns of entries in a specified grid pane
     * @param grid the grid pane to modify
     * @param entries the list of entries conforming the new column
     */
    protected void addColumns(GridPane grid, List<GridEntry> entries){
        int columnIndex = grid.getColumnCount();
        for(GridEntry entry : entries){
            ColumnConstraints constraint = new ColumnConstraints();
            constraint.setPercentWidth(entry.constraint);
            grid.getColumnConstraints().add(constraint);
            grid.addColumn(columnIndex, entry.node);
            columnIndex++;
        }
    }

    /**
     * Adds rows of entries in a specified grid pane
     * @param grid the grid pane to modify
     * @param entries the list of entries conforming the new rows
     */
    protected void addRows(GridPane grid, List<GridEntry> entries){
        int rowIndex = grid.getRowCount();
        for(GridEntry entry : entries){
            RowConstraints constraint = new RowConstraints();
            constraint.setPercentHeight(entry.constraint);
            grid.getRowConstraints().add(constraint);
            grid.addRow(rowIndex, entry.node);
            rowIndex++;
        }
    }
}