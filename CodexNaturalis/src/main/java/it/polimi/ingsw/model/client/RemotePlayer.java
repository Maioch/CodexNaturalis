package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;

import java.util.ArrayList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A class representing the remote player (non-client players)
 */
public class RemotePlayer extends ClientPlayer {
    private ArrayList<BasicCard> handCards; //Not a cardsides list, in order to obtain an only front cards hand

    /**
     * A constructor of the class
     * @param nickname the player's nickname
     */
    public RemotePlayer(String nickname, Content color) {
        super(nickname, color);
        handCards = new ArrayList<>();
    }

    /**
     * Copy-constructor of the class
     * @param remotePlayer the instance to be copied
     */
    public RemotePlayer(RemotePlayer remotePlayer) {
        super(remotePlayer);
        this.handCards = new ArrayList<>(remotePlayer.handCards);
    }

    /**
     * A setter of the hand cards
     * @param handCards the list of the hand cards
     */
    @Override
    public void setHandCards(ArrayList<CardSides> handCards) {
        this.handCards = handCards.stream()
                .map(CardSides::backSide)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * A getter of the hand cards
     * @return the list of the hand cards
     */
    public ArrayList<BasicCard> getHandCards() {
        return new ArrayList<>() {{
            for (BasicCard card : handCards) {
                add(card.copy());
            }
        }};
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof RemotePlayer remotePlayer &&
                remotePlayer.getNickname().equals(this.getNickname()) &&
                remotePlayer.getHandCards().equals(this.getHandCards()) &&
                remotePlayer.getColor().equals(this.getColor());

    }
}