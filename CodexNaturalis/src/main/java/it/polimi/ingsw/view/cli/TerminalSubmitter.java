package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.view.EventSubmitter;

public class TerminalSubmitter implements EventSubmitter {
    @Override
    public void submit(Runnable action){
        action.run();
    }
}
