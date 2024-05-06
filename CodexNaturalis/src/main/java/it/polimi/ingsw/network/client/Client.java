package it.polimi.ingsw.network.client;

import it.polimi.ingsw.view.cli.SetupCLI;

public class Client {
    public static void main(String[] args) {
        if(args.length != 0 && args[0].equals("-cli")){
            SetupCLI cli = new SetupCLI();
        }
    }
}