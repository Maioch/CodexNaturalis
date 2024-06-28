package it.polimi.ingsw.network.shared.messages.setup;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

/**
 * Message sent when a new player joins a game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
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
     *                 or the player's turn number if the status is NEW_PLAYER_JOINED.
     * @param gameId   the ID of the game associated with the join request.
     *
     * @see Content
     */
    public JoinGameMessage(Status status, String nickname, Content color, Integer gameInfo, Integer gameId){
        super(status);
        this.nickname = nickname;
        this.color = color;
        this.gameInfo = gameInfo;
        this.gameId = gameId;
    }

    /**
     * Gets the attached player's nickname.
     *
     * @return the attached player's nickname.
     */
    public String getNickname(){
        return nickname;
    }

    /**
     * Gets the attached player's color.
     *
     * @return the attached player's color.
     *
     * @see Content
     */
    public Content getColor(){
        return color;
    }

    /**
     * Gets the attached game's info.
     *
     * @return the attached game's info.
     */
    public Integer getGameInfo(){
        return gameInfo;
    }

    /**
     * Gets the attached game's id.
     *
     * @return the attached game's id.
     */
    public Integer getGameId(){ return gameId; }
}