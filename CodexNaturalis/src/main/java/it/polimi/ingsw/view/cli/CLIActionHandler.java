package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.core.EventHandler;

/**
 *
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
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