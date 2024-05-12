package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent to handle a show board request.
 */
public class PlayerBoardMessage extends Message {
    private final List<BasicCard> board;
    private final int playerScore;

    /**
     * Constructor for the class.
     * @param board the placed cards of the player.
     */
    public PlayerBoardMessage(List<BasicCard> board, int playerScore){
        super(Status.PLACEMENT_OK);
        this.board = new ArrayList<>(board);
        this.playerScore = playerScore;
    }

    /**
     * @return the player's board, as a list of BasicCards.
     */
    public List<BasicCard> getBoard() {
        return new ArrayList<>(board);
    }

    public int getPlayerScore(){
        return playerScore;
    }
}
