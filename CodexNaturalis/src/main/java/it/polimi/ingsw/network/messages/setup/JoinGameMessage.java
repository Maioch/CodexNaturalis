package it.polimi.ingsw.network.messages.setup;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

/**
 * Message sent when a new player joins a game.
 */
public class JoinGameMessage extends Message {
    private final String nickname;
    private final Content color;
    private final int gameId;

    /**
     * Constructor for the class.
     * @param nickname the player's nickname.
     * @param color the player's color.
     * @param gameId the match the player is joining.
     */
    public JoinGameMessage(Status status, String nickname, Content color, int gameId){
        super(status);
        this.nickname = nickname;
        this.color = color;
        this.gameId = gameId;
    }

    /**
     * @return the player's nickname.
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * @return the player's color.
     */
    public Content getColor(){
        return color;
    }

    /**
     * @return the match's id.
     */
    public int getGameId(){
        return gameId;
    }
}
