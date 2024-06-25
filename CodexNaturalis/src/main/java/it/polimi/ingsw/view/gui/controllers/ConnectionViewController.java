package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Class used to handle the login scene of the GUI.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ConnectionViewController extends ViewController {

    @FXML
    public TextField portTextBox;
    @FXML
    public TextField ipTextBox;
    @FXML
    public Button connectButton;
    @FXML
    public Label errorText;
    @FXML
    public RadioButton tcpRadioButton;
    @FXML
    public RadioButton rmiRadioButton;
    @FXML
    public ToggleGroup protocol;

    //the connection settings chosen by the user
    private ConnectionSettings connectionSettings;

    //the application instance, used to open the rulebook's URL.
    private Application application;

    /**
     * Connects the client to the server, using the information inputted in the login module.
     */
    @FXML
    public void connectButtonHandler(){
        ipTextBox.setDisable(true);
        portTextBox.setDisable(true);
        tcpRadioButton.setDisable(true);
        rmiRadioButton.setDisable(true);
        connectButton.setDisable(true);
        if(!checkAddress(portTextBox.getText())){
            setLoginError("Invalid IP address or Port!");
            return;
        }
        errorText.setText("Connecting...");
        int port = Integer.parseInt(portTextBox.getText());
        new Thread(() ->{
            try {
                ConnectionSettings.ConnectionType type = tcpRadioButton.isSelected() ?
                        ConnectionSettings.ConnectionType.TCP : ConnectionSettings.ConnectionType.RMI;
                connectionSettings = new ConnectionSettings(ipTextBox.getText(), port, type);
                ConnectionInitializer.initializeConnection(connectionSettings, client.getController());
                new Thread(client.getController()).start();
                client.getController().sendMessage(new Message(Status.REQUEST_PING));
                client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
            } catch (TCPException e) {
                setLoginError(e.getMessage());
            } catch (MalformedURLException e) {
                setLoginError("No RMI Server was found at the supplied address");
            } catch (NotBoundException e) {
                setLoginError("The requested object isn't bound");
            } catch (RemoteException e) {
                setLoginError("Couldn't connect to the RMI server");
            }
        }).start();
    }

    /**
     * Gets the current connection settings (chose by the user).
     *
     * @return the current connection settings.
     */
    public ConnectionSettings getConnectionSettings(){
        return connectionSettings;
    }

    /**
     * Sets the error message to be shown to the client in case of incorrect login information.
     *
     * @param errorPrompt the error message to show.
     */
    private void setLoginError(String errorPrompt){
        Platform.runLater(() -> {
            ipTextBox.setDisable(false);
            portTextBox.setDisable(false);
            tcpRadioButton.setDisable(false);
            rmiRadioButton.setDisable(false);
            connectButton.setDisable(false);
            errorText.setText(errorPrompt);
        });
    }

    /**
     * Sets the application attribute
     *
     * @param application the scene's application
     */
    public void setApplication(Application application){ this.application = application; }

    /**
     * Disables the "connect" button if the client hasn't inputted both the ip and the port.
     */
    @FXML
    public void checkIfEnableButton(){
        connectButton.setDisable(ipTextBox.getText().isEmpty() || portTextBox.getText().isEmpty());
    }

    /**
     * Opens the game rules PDF resource.
     */
    @FXML
    public void openRulesLink(){
        application.getHostServices().showDocument(Parameters.getRulesURL());
    }

    /**
     * Checks if the login info inputted by the client are correctly formatted.
     *
     * @param port the port inputted by the client.
     *
     * @return true if the format is correct.
     */
    private boolean checkAddress(String port){
        try{
            int portInt = Integer.parseInt(port);
            if (portInt < 1 || portInt > 65535)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }
}