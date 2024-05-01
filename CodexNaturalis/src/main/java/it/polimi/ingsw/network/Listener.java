package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

public interface Listener {
    public void update(Message message);
}