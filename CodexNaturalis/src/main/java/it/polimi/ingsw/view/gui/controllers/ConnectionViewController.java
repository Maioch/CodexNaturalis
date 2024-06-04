package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.view.gui.GraphicalSubmitter;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.regex.Pattern;

/**
 * Class used to handle the login scene for the GUI.
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

    private Application application;

    /**
     * Method used to handle the client's connection to the server, using the information inputted in the login module.
     */
    @FXML
    public void connectButtonHandler(){
        ipTextBox.setDisable(true);
        portTextBox.setDisable(true);
        tcpRadioButton.setDisable(true);
        rmiRadioButton.setDisable(true);
        connectButton.setDisable(true);
        if(!checkAddress(ipTextBox.getText(), portTextBox.getText())){
            setLoginError("Invalid IP address or Port!");
            return;
        }
        errorText.setText("Connecting...");
        int port = Integer.parseInt(portTextBox.getText());
        if(tcpRadioButton.isSelected()){
            new Thread(() ->{
                try {
                    ConnectionInitializer.initializeTCP(ipTextBox.getText(), port, controller, new GraphicalSubmitter());
                    new Thread(controller).start();
                } catch (IOException e) {
                    setLoginError("Couldn't connect to the specified server");
                }
            }).start();
        } else {
            new Thread(() -> {
                try {
                    ConnectionInitializer.initializeRMI(ipTextBox.getText(), port, controller, new GraphicalSubmitter());
                    new Thread(controller).start();
                } catch (MalformedURLException e) {
                    setLoginError("No RMI Server was found at the supplied address");
                } catch (NotBoundException e) {
                    setLoginError("The requested object isn't bound");
                } catch (RemoteException e) {
                    setLoginError("Couldn't connect to the RMI server");
                }
            }).start();
        }
    }

    /**
     * Method used to handle the error message to be shown to the client in case of incorrect login information.
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
     * Setter for the controller attribute
     * @param application the scene's application
     */
    public void setApplication(Application application){ this.application = application; }

    /**
     * Method used to disable the "connect" button if the client hasn't inputted both the ip and the port.
     */
    @FXML
    public void checkIfEnableButton(){
        connectButton.setDisable(ipTextBox.getText().isEmpty() || portTextBox.getText().isEmpty());
    }

    @FXML
    public void openRulesLink(){
        application.getHostServices().showDocument(GameParameters.getRulesURL());
    }

    /**
     * Method used to check if the login info inputted by the client are correctly formatted.
     * @param ip the ip inputted by the client.
     * @param port the port inputted by the client.
     * @return true if the format is correct.
     */
    private boolean checkAddress(String ip, String port){
        try{
            int portInt = Integer.parseInt(port);
            if (portInt < 1 || portInt > 65535)
                return false;
        } catch (NumberFormatException e) {
            return false;
        }
        return ip.length() <= 15 && Pattern.compile("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0.9]{0,3}").matcher(ip).find();
    }
}