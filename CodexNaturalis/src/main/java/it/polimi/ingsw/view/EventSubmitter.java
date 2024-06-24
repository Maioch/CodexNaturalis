package it.polimi.ingsw.view;

/**
 * Consents to submit an event (which is represented as a runnable).
 * This is used to create queues of chronologically executing actions.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public interface EventSubmitter {

    /**
     * Submits a runnable action.
     *
     * @param action the action to submit.
     */
    void submit(Runnable action);
}