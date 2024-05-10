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
 * A class representing the client's game instance (skinnier than the server's game).
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
     * Constructor for the class.
     * @param player the player associated with the client.
     * @param eventSubmitter the medium used to send the player's requests to the server.
     * @param gameView the object containing all the methods used by the player to interact with the game.
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
     * Setter for the drawable options (cards that the player can draw).
     * @param drawableOptions hashmap containing all the drawable cards.
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions));
    }

    /**
     * Method that requests a new draw move.
     * @param drawableOptions the cards the player can choose from.
     */
    public void requestDraw(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.requestDraw(drawableOptions));
    }

    /**
     * @return a map that contains all the drawable cards.
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
     * @return the local player.
     */
    public LocalPlayer getLocalPlayer() {
        return this.localPlayer;
    }

    /**
     * @return a list of the remote players (part of the game).
     */
    public List<RemotePlayer> getRemotePlayers() {
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    /**
     * @return a map that contains each player's nickname and his associated color.
     */
    public Map<String, Content> getPlayersColors(){
        HashMap<String, Content> playersColors = new HashMap<>();
        playersColors.put(getLocalPlayer().getNickname(), getLocalPlayer().getColor());
        for(RemotePlayer remotePlayer : getRemotePlayers()){
            playersColors.put(remotePlayer.getNickname(), remotePlayer.getColor());
        }
        return playersColors;
    }

    /**
     * A method that updates the views, showing the available colors.
     * @param colors the list of available colors.
     */
    public void updateAvailableColors(List<Content> colors){
        //Update the view with the currently available colors
    }

    /**
     * A method that adds a player to the remote players list.
     */
    public void addRemotePlayer(RemotePlayer player){
        remotePlayers.add(player);
        player.setViewReferences(gameView,eventSubmitter);
        eventSubmitter.submit(() -> gameView.showUserJoined(player.getNickname(), player.getColor()));
    }

    /**
     * Setter for the common objectives (the ones that all the players own).
     * @param commonObjectives the common objective list.
     */
    public void setCommonObjectives(List<Objective> commonObjectives) {
        this.commonObjectives = new ArrayList<>(commonObjectives);
        eventSubmitter.submit(() -> gameView.updateCommonObjectives(getCommonObjectives()));
    }

    /**
     * Setter of the local player (usually used just one time).
     * @param localPlayer the local player.
     */
    public void setLocalPlayer(LocalPlayer localPlayer) {
        this.localPlayer = localPlayer;
    }

    /**
     * Method the manages the game's turns.
     * @param nickname the nickname that should have the turn.
     */
    public void setPlayerWithTurn(String nickname) {
        ClientPlayer remotePlayerWithTurn = remotePlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);
        playerWithTurn = remotePlayerWithTurn == null ? localPlayer : remotePlayerWithTurn;
        gameView.turnChanged(nickname);
    }

    /**
     * @return the player's common objectives.
     */
    public List<Objective> getCommonObjectives(){
        return new ArrayList<>(){{
            for(Objective obj : commonObjectives){
                add(new Objective(obj));
            }
        }};
    }

    /**
     * @return the player that has the turn.
     */
    public ClientPlayer getPlayerWithTurn(){
        return playerWithTurn;
    }
}