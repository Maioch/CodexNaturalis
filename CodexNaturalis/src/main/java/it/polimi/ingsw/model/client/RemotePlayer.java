package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * RemotePlayer is one of the players connected to the same game as the local client.
 * It extends ClientPlayer and adds the player's hand cards, but only as a BasicCards array, as the only needed sides
 * are the ones that can be seen by the other players.
 *
 * @see BasicCard
 * @see ClientPlayer
 */
public class RemotePlayer extends ClientPlayer {

    private List<BasicCard> handCards;

    /**
     * Class constructor.
     *
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    public RemotePlayer(String nickname, Content color) {
        super(nickname, color);
        handCards = new ArrayList<>();
    }

    /**
     * Class copy-constructor.
     *
     * @param remotePlayer the instance to be copied.
     */
    public RemotePlayer(RemotePlayer remotePlayer) {
        super(remotePlayer);
        this.handCards = new ArrayList<>(remotePlayer.handCards);
    }

    @Override
    public synchronized void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = handCards.stream()
                .map(CardSides::backSide)
                .collect(Collectors.toCollection(ArrayList::new));
        if(show){
            eventSubmitter.submit(() -> gameView.updateRemotePlayerHand(getNickname(), getHandCards()));
        }
    }

    /**
     * Returns the cards held by the player in his hand.
     *
     * @return the player's hand cards.
     */
    public synchronized List<BasicCard> getHandCards() {
        return new ArrayList<>() {{
            for (BasicCard card : handCards) {
                add(card.copy());
            }
        }};
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param obj player checked.
     *
     * @return true if this player is equal to the parameter one.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemotePlayer remotePlayer &&
                remotePlayer.getNickname().equals(this.getNickname()) &&
                remotePlayer.getHandCards().equals(this.getHandCards()) &&
                remotePlayer.getColor().equals(this.getColor());

    }
}