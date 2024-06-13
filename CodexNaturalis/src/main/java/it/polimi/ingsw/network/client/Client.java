package it.polimi.ingsw.network.client;

import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.cli.SetupCLI;
import it.polimi.ingsw.view.gui.CodexApplication;
import javafx.application.Application;

/**
 * Main class used to run the client.
 */
public class Client {
    private final SetupView setupView;
    private final EventSubmitter eventSubmitter;
    private ClientController controller;
    private ConnectionSettings connectionSettings;

    public Client(EventSubmitter eventSubmitter, SetupView setupView){
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
    }

    public ClientController getController() {
        return controller;
    }

    public void createController() {
        this.controller = new ClientController(setupView, eventSubmitter);
    }

    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    public static void main(String[] args) {
        if(args.length != 0 && args[0].equals("-cli")){
            new SetupCLI().startCLI();
        }else{
            Application.launch(CodexApplication.class, args);
        }
    }
}