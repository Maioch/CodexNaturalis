package it.polimi.ingsw.network.client;

import it.polimi.ingsw.view.cli.SetupCLI;
import it.polimi.ingsw.view.gui.SetupGUI;
import javafx.application.Application;

/**
 * Main class used to run the client.
 */
public class Client {
    public static void main(String[] args) {
        if(args.length != 0 && args[0].equals("-cli")){
            new SetupCLI();
        }else{
            Application.launch(SetupGUI.class, args);
        }
    }
}