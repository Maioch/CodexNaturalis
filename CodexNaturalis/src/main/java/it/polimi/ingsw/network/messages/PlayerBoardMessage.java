package it.polimi.ingsw.network.messages;

import it.polimi.ingsw.server.model.card.BasicCard;

import java.util.ArrayList;

/**
 * A message that sends a certain player's board (placed cards)
 */
public class PlayerBoardMessage extends Message {
    private final ArrayList<BasicCard> board;

    /**
     * Constructor of the class
     * @param board the placed cards of the player
     */
    public PlayerBoardMessage(ArrayList<BasicCard> board){
        super(Status.PLAYER_BOARD);
        this.board = board;
    }

    /**
     * A getter method for the board attribute
     * @return the player's board, as an ArrayList of BasicCards
     */
    public ArrayList<BasicCard> getBoard() {
        return board;
    }
}
