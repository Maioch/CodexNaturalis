package it.polimi.ingsw.core;

import it.polimi.ingsw.controller.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.cli.SetupCLI;
import it.polimi.ingsw.view.gui.CodexApplication;
import javafx.application.Application;

/**
 * Main class used to run the client. It also contains basic information about it.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Client {

    //the view that's used when not in a game (persists throughout the entire lifecycle of the program).
    private final SetupView setupView;

    //the event submitter for the type of interface chosen by the user.
    private final EventSubmitter eventSubmitter;

    //the current controller.
    private ClientController controller;

    //stores information about the connection to the server, if the client was already connected to any.
    private ConnectionSettings connectionSettings;

    /**
     * Class constructor.
     *
     * @param eventSubmitter the event submitter used by the client.
     * @param setupView      the current setup view used by the client.
     *
     * @see EventSubmitter
     * @see SetupView
     */
    public Client(EventSubmitter eventSubmitter, SetupView setupView){
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
    }

    /**
     * Gets the client controller instance.
     *
     * @return the client controller instance.
     *
     * @see ClientController
     */
    public ClientController getController() {
        return controller;
    }

    /**
     * Creates a new client controller instance.
     * The client controller is used to manage messages received.
     *
     * @see ClientController
     */
    public void createController() {
        this.controller = new ClientController(setupView, eventSubmitter);
    }

    /**
     * Sets the current connection settings of the client, such as server's IP, port and connection type.
     *
     * @param connectionSettings the settings to set.
     *
     * @see ConnectionSettings
     */
    public void setConnectionSettings(ConnectionSettings connectionSettings) {
        this.connectionSettings = connectionSettings;
    }

    /**
     * Gets the current connection settings of the client.
     *
     * @return the current connection settings.
     *
     * @see ConnectionSettings
     */
    public ConnectionSettings getConnectionSettings() {
        return connectionSettings;
    }

    /**
     * Main method, entry point for the client.
     * This starts the application using either the CLI view or the GUI one.
     *
     * @param args the arguments given with the execution of the client application.
     *             If there's no argument, the application is run using the GUI.
     *             To start the application using the CLI, run with "-cli" parameter.
     */
    public static void main(String[] args) {
        if(args.length != 0 && args[0].equals("-cli")){
            new SetupCLI().startCLI();
        }else{
            Application.launch(CodexApplication.class, args);
        }
    }
}