package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

/**
 *
 */
public class LoginViewController {

    @FXML
    public TextField portTextBox;

    @FXML
    public TextField ipTextBox;

    @FXML
    public RadioButton tcpRadioButton;

    @FXML
    public Button connectButton;

    @FXML
    public Label errorText;

    private ClientController controller;

    @FXML
    public void connectButtonHandler(){
        errorText.setVisible(false);
        if(!checkAddress(ipTextBox.getText(), portTextBox.getText())){
            errorText.setVisible(true);
            errorText.setText("Invalid IP address or Port!");
            return;
        }
        errorText.setText("Connecting...");
        int port = Integer.parseInt(portTextBox.getText());
        if(tcpRadioButton.isSelected()){
            try {
                ConnectionInitializer.initializeTCP(ipTextBox.getText(), port, controller);
            } catch (IOException e) {
                errorText.setVisible(true);
                errorText.setText("Could not connect the specified server");
            }
        }else{
            try {
                ConnectionInitializer.initializeRMI(ipTextBox.getText(), port, controller);
            } catch (MalformedURLException e) {
                errorText.setVisible(true);
                errorText.setText("No RMI Server was found at the supplied address");
            } catch (NotBoundException e) {
                errorText.setVisible(true);
                errorText.setText("The requested object isn't bound");
            } catch (RemoteException e) {
                errorText.setVisible(true);
                errorText.setText("Couldn't connect to the RMI server");
            }
        }
    }

    public void setClientController(ClientController controller){
        this.controller = controller;
    }

    @FXML
    public void checkIfEnableButton(){
        connectButton.setDisable(ipTextBox.getText().isEmpty() || portTextBox.getText().isEmpty());
    }

    private boolean checkAddress(String ip, String port){
        try{
            Integer.parseInt(port);
        } catch (NumberFormatException e) {
            return false;
        }
        return ip.length() <= 15 && Pattern.compile("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0.9]{0,3}").matcher(ip).find();
    }
}