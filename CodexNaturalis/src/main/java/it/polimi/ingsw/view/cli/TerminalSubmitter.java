package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.view.EventSubmitter;

public class TerminalSubmitter implements EventSubmitter {

    //the handler that is going to receive the submitted events.
    private final CLIActionHandler CLIActionHandler;

    public TerminalSubmitter(CLIActionHandler CLIActionHandler) {
        this.CLIActionHandler = CLIActionHandler;
    }

    @Override
    public void submit(Runnable action){
        CLIActionHandler.addEventToQueue(action);
    }
}