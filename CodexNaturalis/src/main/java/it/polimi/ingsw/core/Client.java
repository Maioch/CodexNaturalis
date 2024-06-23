package it.polimi.ingsw.core;

import it.polimi.ingsw.controller.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionSettings;
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

    /**
     * Gets the client's controller instance.
     *
     * @return client's controller instance.
     */
    public ClientController getController() {
        return controller;
    }

    /**
     * Creates (initializes) the client's controller instance.
     * The client's controller is used to manage messages received and run view methods.
     */
    public void createController() {
        this.controller = new ClientController(setupView, eventSubmitter);
    }

    /**
     * Sets the current connection settings of the client, such as server's IP, port and connection type.
     *
     * @param connectionSettings the settings to set.
     */
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    /**
     * Gets the current connection settings of the client.
     *
     * @return the current connection settings.
     */
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    /**
     * Main method, entry point for the client.
     * This starts the application using either the CLI view or the GUI one.
     *
     * @param args the arguments given with the execution of the client application.
     *             If there's no argument, or simply the first one isn't "-cli", the application is run using the GUI.
     */
    public static void main(String[] args) {
        if(args.length != 0 && args[0].equals("-cli")){
            new SetupCLI().startCLI();
        }else{
            Application.launch(CodexApplication.class, args);
        }
    }
}