package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;

import java.util.ArrayList;

public class ValidPlacementsMessage extends Message {
    ArrayList<Corner> corners;
    ArrayList<BasicCard> cards;

    public ValidPlacementsMessage(Status status,ArrayList<BasicCard> cards, ArrayList<Corner> corners) {
        super(status);
        this.corners = corners;
        this.cards = cards;
    }

    public ArrayList<Corner> getPlaceableCorners(){
        return new ArrayList<>(corners);
    }

    public ArrayList<BasicCard> getPlaceableCards(){
        return new ArrayList<>(cards);
    }
}