package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

public class JoinGameMessage extends Message {
    private final String nickname;
    private final Content color;
    private final int gameId;

    /**
     * Constructor for the class
     * @param nickname the player's nickname
     * @param color the player's color
     */
    public JoinGameMessage(String nickname, Content color, int gameId){
        super(Status.JOIN_GAME);
        this.nickname = nickname;
        this.color = color;
        this.gameId = gameId;
    }

    /**
     * Getter method for the string sent along the message
     * @return string attribute
     */
    public String getNickname(){
        return nickname;
    }

    public Content getColor(){
        return color;
    }

    public int getGameId(){
        return gameId;
    }
}
