package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.card.BasicCard;

import java.util.ArrayList;

public abstract class ClientPlayer {
    private final String nickname;
    private ArrayList<BasicCard> placedCards;
    private int score;

    public ClientPlayer(String nickname) {
        this.nickname = nickname;
        this.score = 0;
        this.placedCards = new ArrayList<>();
    }

    public ClientPlayer(ClientPlayer clientPlayer) {
        this.nickname = clientPlayer.getNickname();
        this.placedCards = new ArrayList<>(clientPlayer.getPlacedCards());
        this.score = clientPlayer.getScore();
    }

    public String getNickname() {
        return nickname;
    }

    public ArrayList<BasicCard> getPlacedCards() {
        return new ArrayList<>(){{
            for(BasicCard card : placedCards){
                add(card.copy());
            }
        }};
    }

    public void setPlacedCards(ArrayList<BasicCard> placedCards) {
        this.placedCards = placedCards;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

}
