package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ClientGame {
    private final LocalPlayer localPlayer;
    private final ArrayList<RemotePlayer> remotePlayers;
    private ArrayList<Objective> commonObjectives;
    private HashMap<CardType, ArrayList<BasicCard>> drawableOptions;

    public ClientGame(LocalPlayer localPlayer) {
        this.localPlayer = localPlayer;
        this.remotePlayers = new ArrayList<>();
        this.commonObjectives = new ArrayList<>();
    }

    public void setDrawableOptions(HashMap<CardType, ArrayList<BasicCard>> drawableOptions) {
        this.drawableOptions = drawableOptions;
    }

    public HashMap<CardType, ArrayList<BasicCard>> getDrawableOptions() {
        return new HashMap<>(){{
            for(Map.Entry<CardType,ArrayList<BasicCard>> entry : drawableOptions.entrySet()) {
                ArrayList<BasicCard> newValue = new ArrayList<BasicCard>(){{
                    for(BasicCard card : entry.getValue()){
                        add(card.copy());
                    }
                }};
                put(entry.getKey(),newValue);
            }
        }};
    }

    public LocalPlayer getLocalPlayer() {
        return this.localPlayer;
    }

    public ArrayList<RemotePlayer> getRemotePlayers() {
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    public void updateAvailableColors(ArrayList<Content> colors){
        //Update the view with the currently available colors
    }

    public void addRemotePlayer(String nickname){
        remotePlayers.add(new RemotePlayer(nickname));
    }

    public void setCommonObjectives(ArrayList<Objective> commonObjectives) {
        this.commonObjectives = commonObjectives;
    }
}
