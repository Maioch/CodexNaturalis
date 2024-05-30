package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.view.EventSubmitter;

public class TerminalSubmitter implements EventSubmitter {
    private final CLIActionHandler CLIActionHandler;

    public TerminalSubmitter(CLIActionHandler CLIActionHandler) {
        this.CLIActionHandler = CLIActionHandler;
    }

    @Override
    public void submit(Runnable action){
        CLIActionHandler.addEventToQueue(action);
    }
}