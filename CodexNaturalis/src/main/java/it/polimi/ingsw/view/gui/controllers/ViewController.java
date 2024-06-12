package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.network.client.Client;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Abstract class that implements some recurrent view controller methods.
 */
public abstract class ViewController {
    protected record DisconnectionControls(GridPane popupGrid, Label label, Button button){}
    protected Client client;
    private DisconnectionControls disconnectionControls;

    public void setClient(Client client) {
        this.client = client;
    }

    protected void setDisconnectionControls(DisconnectionControls disconnectionControls){
        this.disconnectionControls = disconnectionControls;
    }

    public void showReconnectionError(String error){
        disconnectionControls.label().setText(error);
        disconnectionControls.button().setOnMouseClicked((mouseEvent ->
                client.getController().sendMessage(new Message(Status.REQUEST_GAMES))));
        disconnectionControls.button().setText("Back to match list");
        disconnectionControls.popupGrid().setVisible(true);
    }

    /**
     * Handles the client's disconnection. Shows the reconnection popup.
     * @param messageToSend the message to send for eventual reconnection.
     */
    public void handleDisconnection(Message messageToSend){
        disconnectionControls.button().setOnMouseClicked((mouseEvent ->
                tryReconnectToServer(client.getConnectionSettings(), messageToSend)));
        disconnectionControls.popupGrid().setVisible(true);
    }

    /**
     * Tries to reconnect to the server.
     * @param connectionSettings the connection settings to use for reconnection.
     * @param messageToSend the message to send in order to reconnect.
     */
    protected void tryReconnectToServer(ConnectionSettings connectionSettings, Message messageToSend){
        disconnectionControls.button().setDisable(true);
        new Thread(() ->{
            try {
                ConnectionInitializer.initializeConnection(connectionSettings, client.getController());
                new Thread(client.getController()).start();
                client.getController().sendMessage(new Message(Status.REQUEST_PING));
                client.getController().sendMessage(messageToSend);
            } catch (TCPException | MalformedURLException | NotBoundException | RemoteException e) {
                Platform.runLater(() -> disconnectionControls.label().setText("Could not reconnect to the server. Try again"));
            }
            Platform.runLater(() -> disconnectionControls.button().setDisable(false));
        }).start();
    }

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