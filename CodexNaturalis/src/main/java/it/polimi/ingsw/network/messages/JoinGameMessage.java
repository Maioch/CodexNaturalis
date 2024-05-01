package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.model.server.Content;

public class JoinGameMessage extends Message{
    private final String nickname;
    private final Content color;
    private final int roomId;

    /**
     * Constructor for the class
     * @param nickname the player's nickname
     * @param color the player's color
     */
    public JoinGameMessage(String nickname, Content color, int roomId) {
        super(Status.JOIN_GAME);
        this.nickname = nickname;
        this.color = color;
        this.roomId = roomId;
    }

    /**
     * Getter method for the string sent along the message
     * @return string attribute
     */
    public String getNickname() {
        return nickname;
    }

    public Content getColor() { return color; }

    public int getRoomId() {
        return roomId;
    }
}
