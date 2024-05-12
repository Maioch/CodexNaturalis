package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * A message used to show the information about a player.
 */
public class PlayerMessage extends Message {
    private final String nickname;
    private final Content color;

    /**
     * Constructor for the class.
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    public PlayerMessage(Status status, String nickname, Content color) {
        super(status);
        this.nickname = nickname;
        this.color = color;
    }

    /**
     * @return the player's nickname.
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * @return the player's color.
     */
    public Content getColor() { return color; }
}
