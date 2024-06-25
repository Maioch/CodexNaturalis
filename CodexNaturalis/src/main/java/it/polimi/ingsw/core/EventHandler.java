package it.polimi.ingsw.core;

import java.util.LinkedList;
import java.util.Queue;

/**
* Handles events following the FIFO principle by saving them in a queue.
*
* @author Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli, Marco Maiocchi
*/
public abstract class EventHandler<T> implements Runnable{

    //stores the events which have yet to be handled.
    private final Queue<T> eventQueue;

    /**
     * Constructor for the class.
     */
    public EventHandler(){
        eventQueue = new LinkedList<>();
    }

    /**
     * Enqueues an event to the events queue.
     *
     * @param event the event to add.
     */
    public synchronized void addEventToQueue(T event){
        eventQueue.add(event);
    }

    /**
     * Gets and dequeues an event from the queue.
     *
     * @return the read event.
     */
    public synchronized T getEventFromQueue(){
        return eventQueue.poll();
    }
}