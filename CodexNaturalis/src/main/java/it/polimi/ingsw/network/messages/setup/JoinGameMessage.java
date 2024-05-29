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
    private final int gameInfo;

    /**
     * Constructor for the class.
     * @param nickname the player's nickname.
     * @param color the player's color.
     * @param gameInfo depending on the usage, it can be the id of the match the player is joining
     *                 or the newly created game's number of players.
     */
    public JoinGameMessage(Status status, String nickname, Content color, int gameInfo){
        super(status);
        this.nickname = nickname;
        this.color = color;
        this.gameInfo = gameInfo;
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
     * @return the game's info
     */
    public int getGameInfo(){
        return gameInfo;
    }
}
