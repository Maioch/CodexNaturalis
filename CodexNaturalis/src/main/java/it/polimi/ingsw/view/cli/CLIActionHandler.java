package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.network.EventHandler;

public class CLIActionHandler extends EventHandler<Runnable>{
    /**
     * Constructor for the class.
     */
    public CLIActionHandler(){
        super();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run(){
        while(true){
            Runnable action = getEventFromQueue();
            if(action == null){
                continue;
            }
            action.run();
        }
    }
}