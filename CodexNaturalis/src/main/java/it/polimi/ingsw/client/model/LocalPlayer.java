package it.polimi.ingsw.client.model;

import it.polimi.ingsw.server.model.card.BasicCard;
import it.polimi.ingsw.server.model.card.CardSides;
import it.polimi.ingsw.server.model.card.Objective;

import java.util.ArrayList;

public class LocalPlayer extends ClientPlayer{
    private ArrayList<CardSides> handCards;
    private final ArrayList<Objective> objectives;

    public LocalPlayer(String nickname, ArrayList<Objective> objectives) {
        super(nickname);
        this.objectives = objectives;
    }

    public ArrayList<CardSides> getHandCards() {
        return new ArrayList<>(){{
            for(CardSides cardSides : handCards){
                add(new CardSides(
                        cardSides.frontSide().copy(),
                        cardSides.backSide().copy()));
            }
        }};
    }

    public void setHandCards(ArrayList<CardSides> handCards) {
        this.handCards = handCards;
    }

    public ArrayList<Objective> getObjectives() {
        return objectives;
    }
}
