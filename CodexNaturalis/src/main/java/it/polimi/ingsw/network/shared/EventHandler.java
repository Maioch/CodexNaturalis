package it.polimi.ingsw.network.shared;

import java.util.LinkedList;
import java.util.Queue;

/**
* Abstract class that handles events by saving them in a queue.
*
* @author Andrea Fidanza, Guglielmo Gatti, Francesco Nisoli, Marco Maiocchi
*/
public abstract class EventHandler<T> implements Runnable{
    private final Queue<T> eventQueue;

    /**
     * Constructor for the class.
     */
    public EventHandler(){
        eventQueue = new LinkedList<>();
    }

    /**
     * Synchronized method that adds an event to the events queue.
     * @param event the event to add.
     */
    public synchronized void addEventToQueue(T event){
        eventQueue.add(event);
    }

    /**
     * Synchronized method that reads and removes the oldest event from the queue.
     * @return the read event.
     */
    public synchronized T getEventFromQueue(){
        return eventQueue.poll();
    }
}