package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class representing the remote player.
 */
public class RemotePlayer extends ClientPlayer {
    private List<BasicCard> handCards; //Not a card sides list, in order to obtain an only front cards hand

    /**
     * Constructor for the class.
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    public RemotePlayer(String nickname, Content color) {
        super(nickname, color);
        handCards = new ArrayList<>();
    }

    /**
     * Copy-constructor for the class.
     * @param remotePlayer the instance to be copied.
     */
    public RemotePlayer(RemotePlayer remotePlayer) {
        super(remotePlayer);
        this.handCards = new ArrayList<>(remotePlayer.handCards);
    }

    /**
     * Setter for the hand cards attribute.
     * @param handCards the player's hand.
     */
    @Override
    public void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = handCards.stream()
                .map(CardSides::backSide)
                .collect(Collectors.toCollection(ArrayList::new));
        if(show){
            eventSubmitter.submit(() -> gameView.updateRemotePlayerHand(getNickname(), getHandCards()));
        }
    }

    /**
     * @return the list of the player's hand cards.
     */
    public List<BasicCard> getHandCards() {
        return new ArrayList<>() {{
            for (BasicCard card : handCards) {
                add(card.copy());
            }
        }};
    }

    /**
     * Method that checks if two remote players are equal.
     * @param obj the remote player to check.
     * @return true if this and the other remote player are equal.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemotePlayer remotePlayer &&
                remotePlayer.getNickname().equals(this.getNickname()) &&
                remotePlayer.getHandCards().equals(this.getHandCards()) &&
                remotePlayer.getColor().equals(this.getColor());

    }
}