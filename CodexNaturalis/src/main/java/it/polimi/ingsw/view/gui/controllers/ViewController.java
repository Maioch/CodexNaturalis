package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
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
 * Implements recurrent view controller methods.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public abstract class ViewController {

    /**
     * Stores the nodes that are shown whenever the client gets disconnected from the server.
     *
     * @param popupGrid the popup shown whenever a disconnection happens.
     * @param label     the label on error message.
     * @param button    the button used to reconnect to the server.
     */
    protected record DisconnectionControls(GridPane popupGrid, Label label, Button button){}

    //the client instance used for the entire program's lifecycle
    protected Client client;

    //used to store the nodes that are shown whenever the client gets disconnected from the server.
    private DisconnectionControls disconnectionControls;

    /**
     * Sets the client instance related to the GUI.
     *
     * @param client the Client instance to set.
     *
     * @see Client
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Sets the disconnection controls.
     *
     * @param disconnectionControls the disconnection controls to set (popup, label, button).
     */
    protected void setDisconnectionControls(DisconnectionControls disconnectionControls){
        this.disconnectionControls = disconnectionControls;
    }

    /**
     * Shows a reconnection-error pop up
     *
     * @param error the error message to show.
     */
    public void showReconnectionError(String error){
        disconnectionControls.label().setText(error);
        disconnectionControls.button().setOnMouseClicked((mouseEvent ->
                client.getController().sendMessage(new Message(Status.REQUEST_GAMES))));
        disconnectionControls.button().setText("Back to match list");
        disconnectionControls.popupGrid().setVisible(true);
    }

    /**
     * Handles the client's disconnection. Shows the reconnection popup.
     *
     * @param messageToSend the message to send for eventual reconnection.
     *
     * @see Message
     */
    public void handleDisconnection(Message messageToSend){
        disconnectionControls.button().setOnMouseClicked((mouseEvent ->
                tryReconnectToServer(client.getConnectionSettings(), messageToSend)));
        disconnectionControls.popupGrid().setVisible(true);
    }

    /**
     * Tries to reconnect to the server.
     *
     * @param connectionSettings the connection settings to use for reconnection.
     * @param messageToSend      the message to send in order to reconnect.
     *
     * @see ConnectionSettings
     * @see Message
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
     *
     * @param buttonText the contained in button text.
     * @param group      the button's group.
     * @param styleClass the css style class
     *
     * @return           the created radio button.
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
     *
     * @return           the created radio button.
     */
    protected RadioButton createRadioButton(String buttonText, ToggleGroup group){
        RadioButton radioButton = new RadioButton(buttonText);
        radioButton.setToggleGroup(group);
        radioButton.setMaxWidth(Double.MAX_VALUE);
        return radioButton;
    }

    /**
     * Represents a grid entry (with its constraint value, and the node in it).
     *
     * @param constraint the integer value of the constraint (percentage)
     * @param node the entry node
     */
    protected record GridEntry(int constraint, Node node){}

    /**
     * Adds columns of entries in a specified grid pane
     *
     * @param grid    the grid pane to modify
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
     *
     * @param grid    the grid pane to modify
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