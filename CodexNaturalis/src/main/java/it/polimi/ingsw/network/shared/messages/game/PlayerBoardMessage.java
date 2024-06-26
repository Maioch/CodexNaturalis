package it.polimi.ingsw.network.shared.messages.game;

import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Message sent to handle a show board request.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class PlayerBoardMessage extends Message {
    private final List<BasicCard> board;
    private final int playerScore;

    /**
     * Constructor for the class.
     *
     * @param board       the placed cards of the player.
     * @param playerScore the player's score.
     *
     * @see BasicCard
     */
    public PlayerBoardMessage(List<BasicCard> board, int playerScore){
        super(Status.PLACEMENT_OK);
        this.board = new ArrayList<>(board);
        this.playerScore = playerScore;
    }

    /**
     * Gets the attached player's board.
     *
     * @return the attached player's board, as a list of BasicCards.
     *
     * @see BasicCard
     */
    public List<BasicCard> getBoard() {
        return new ArrayList<>(board);
    }

    /**
     * Gets the attached player's score.
     *
     * @return the attached player's score.
     */
    public int getPlayerScore(){
        return playerScore;
    }
}