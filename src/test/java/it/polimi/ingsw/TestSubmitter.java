package it.polimi.ingsw;

import it.polimi.ingsw.view.EventSubmitter;

public class TestSubmitter implements EventSubmitter {

    public TestSubmitter() {}

    public void submit(Runnable runnable) {
        runnable.run();
    }
}