package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

public interface Listener {
    void update(Message message);
}