package it.polimi.ingsw.network.messages.game;

import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.ArrayList;

public class PlaceableCornersMessage {
    ArrayList<Corner> corners;

    public PlaceableCornersMessage(ArrayList<Corner> corners) {
        this.corners = corners;
    }

    public ArrayList<Corner> getPlaceableCorners(){
        return new ArrayList<>();
    }
}
