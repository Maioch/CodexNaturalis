package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.card.BasicCard;

import java.util.ArrayList;

public class RemotePlayer extends ClientPlayer {
    private ArrayList<BasicCard> handCards;

    public RemotePlayer(String nickname) {
        super(nickname);
        handCards = new ArrayList<>();
    }

    public RemotePlayer(RemotePlayer remotePlayer) {
        super(remotePlayer);
        this.handCards = new ArrayList<>(remotePlayer.handCards);
    }

    public void setHandCards(ArrayList<BasicCard> handCards) {
        this.handCards = handCards;
    }

    public ArrayList<BasicCard> getHandCards() {
        return new ArrayList<>() {{
            for (BasicCard card : handCards) {
                add(card.copy());
            }
        }};
    }
}