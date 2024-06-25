package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.core.EventHandler;

/**
 * Manages the queue of CLI events, with a FIFO policy.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CLIActionHandler extends EventHandler<Runnable>{

    /**
     * Class constructor.
     */
    public CLIActionHandler(){
        super();
    }

    /**
     * Gets the oldest event in the queue and runs its related action, if present.
     */
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