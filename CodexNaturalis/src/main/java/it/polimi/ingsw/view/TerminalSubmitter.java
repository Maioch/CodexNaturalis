package it.polimi.ingsw.view;

public class TerminalSubmitter implements EventSubmitter{
    @Override
    public void submit(Runnable action){
        action.run();
    }
}
