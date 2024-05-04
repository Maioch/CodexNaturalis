package it.polimi.ingsw.view;

public interface EventSubmitter {
    void submit(Runnable action);
}