package it.polimi.ingsw.network.client;

import it.polimi.ingsw.view.SetupView;
import it.polimi.ingsw.view.cli.SetupCLI;

public class Client {
    public static void main(String[] args) {
        SetupView setupView = (args.length != 0 && args[0].equals("-cli")) ? new SetupCLI() : null;
    }
}