package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class representing the client's game instance (skinnier than the server's game)
 */

public class ClientGame {
    private LocalPlayer localPlayer;
    private final List<RemotePlayer> remotePlayers;
    private List<Objective> commonObjectives;
    private Map<CardType, List<BasicCard>> drawableOptions;
    private ClientPlayer playerWithTurn;
    private final EventSubmitter eventSubmitter;
    private final GameView gameView;


    /**
     * Constructor of the client's game instance
     */
    public ClientGame(LocalPlayer player, EventSubmitter eventSubmitter, GameView gameView) {
        this.localPlayer = player;
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
        this.remotePlayers = new ArrayList<>();
        this.commonObjectives = new ArrayList<>();
        this.playerWithTurn = null;
    }

    /**
     * Setter of the drawable options (cards which the player can draw)
     * @param drawableOptions hashmap containing the card type and the relative cards (of
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions));
    }

    public void requestDraw(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.requestDraw(drawableOptions));
    }

    /**
     * Getter of the drawable cards
     * @return an hashmap representing the drawable cards
     */
    public Map<CardType, List<BasicCard>> getDrawableOptions() {
        return new HashMap<>(){{
            for(Map.Entry<CardType,List<BasicCard>> entry : drawableOptions.entrySet()) {
                List<BasicCard> newValue = new ArrayList<>(){{
                    for(BasicCard card : entry.getValue()){
                        add(card.copy());
                    }
                }};
                put(entry.getKey(),newValue);
            }
        }};
    }

    /**
     * Getter of the local player attribute
     * @return the local player
     */
    public LocalPlayer getLocalPlayer() {
        return this.localPlayer;
    }

    /**
     * Getter of the remote players
     * @return an arraylist of the remote players (part of the game)
     */
    public List<RemotePlayer> getRemotePlayers() {
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    /**
     * A method that updates the vies, showing the avaible colors
     * @param colors the list of available colors
     */
    public void updateAvailableColors(List<Content> colors){
        //Update the view with the currently available colors
    }

    /**
     * A method that adds a player to the remote players' list
     */
    public void addRemotePlayer(RemotePlayer player){
        remotePlayers.add(player);
        player.setViewReferences(gameView,eventSubmitter);
        eventSubmitter.submit(() -> gameView.showUserJoined(player.getNickname(), player.getColor()));
    }

    /**
     * Setter of the common objects (the ones that all the players onw)
     * @param commonObjectives the common objective list
     */
    public void setCommonObjectives(List<Objective> commonObjectives) {
        this.commonObjectives = new ArrayList<>(commonObjectives);
        eventSubmitter.submit(() -> gameView.updateCommonObjectives(getCommonObjectives()));
    }

    /**
     * Setter of the local player (usually used just one time)
     * @param localPlayer the local player
     */
    public void setLocalPlayer(LocalPlayer localPlayer) {
        this.localPlayer = localPlayer;
    }

    public void setPlayerWithTurn(String nickname) {
        ClientPlayer remotePlayerWithTurn = remotePlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);
        playerWithTurn = remotePlayerWithTurn == null ? localPlayer : remotePlayerWithTurn;
        gameView.turnChanged(nickname);
    }

    public List<Objective> getCommonObjectives(){
        return new ArrayList<>(){{
            for(Objective obj : commonObjectives){
                add(new Objective(obj));
            }
        }};
    }

    public ClientPlayer getPlayerWithTurn(){
        return playerWithTurn;
    }
}