package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.view.EventSubmitter;

/**
 * Submits events for the CLI.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class TerminalSubmitter implements EventSubmitter {

    //the handler that is going to receive the submitted events.
    private final CLIActionHandler CLIActionHandler;

    /**
     * Constructor of the submitter.
     *
     * @param CLIActionHandler the handler that is going to receive the submitted events.
     */
    public TerminalSubmitter(CLIActionHandler CLIActionHandler) {
        this.CLIActionHandler = CLIActionHandler;
    }

    /**
     * Submits a new action.
     *
     * @param action the action to submit.
     */
    @Override
    public void submit(Runnable action){
        CLIActionHandler.addEventToQueue(action);
    }
}