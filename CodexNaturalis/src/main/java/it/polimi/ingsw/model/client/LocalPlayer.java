package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.Objective;

import java.util.ArrayList;

public class LocalPlayer extends ClientPlayer{
    private ArrayList<CardSides> handCards;
    private ArrayList<Objective> objectives;

    public LocalPlayer(String nickname) {
        super(nickname);
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

    public void setObjectives(ArrayList<Objective> objectives) {
        this.objectives = objectives;
    }

    public ArrayList<Objective> getObjectives() {
        return objectives;
    }
}
