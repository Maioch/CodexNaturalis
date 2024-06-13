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
    private final Integer gameInfo;
    private final Integer gameId;

    /**
     * Class constructor.
     *
     * @param status   the status of the message.
     * @param nickname the player's nickname.
     * @param color    the player's color.
     * @param gameInfo the game's number of players if the status is JOIN_GAME,
     *                 or the player's turn number if the status is NEW_PLAYER_JOINED
     * @param gameId   the ID of the game associated with the join request
     */
    public JoinGameMessage(Status status, String nickname, Content color, Integer gameInfo, Integer gameId){
        super(status);
        this.nickname = nickname;
        this.color = color;
        this.gameInfo = gameInfo;
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
     * @return the game's info
     */
    public Integer getGameInfo(){
        return gameInfo;
    }

    public Integer getGameId(){ return gameId; }
}