package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.view.gui.SetupGUI;
import javafx.scene.Node;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.TextAlignment;

import java.util.List;

public abstract class ViewController {
    protected ClientController controller;

    /**
     * Sets the client controller.
     * @param controller the client controller.
     */
    public void setController(ClientController controller) {
        this.controller = controller;
    }

    public abstract void handleDisconnection(ClientController controller, ConnectionSettings connectionSettings);

    /**
     * Creates a radio button.
     * @param buttonText the contained in button text.
     * @param group the button's group.
     * @param styleClass the css style class
     * @return the created radio button.
     */
    protected RadioButton createRadioButton(String buttonText, ToggleGroup group, String styleClass){
        RadioButton radioButton = createRadioButton(buttonText, group);
        radioButton.getStyleClass().add(styleClass);
        return radioButton;
    }

    /**
     * Creates a radio button.
     *
     * @param buttonText the contained in button text.
     * @param group      the button's group.
     * @return           the created radio button.
     */
    protected RadioButton createRadioButton(String buttonText, ToggleGroup group){
        RadioButton radioButton = new RadioButton(buttonText);
        radioButton.setToggleGroup(group);
        radioButton.setMaxWidth(Double.MAX_VALUE);
        return radioButton;
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