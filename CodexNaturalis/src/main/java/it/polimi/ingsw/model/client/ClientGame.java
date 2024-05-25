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
 * Class that represents a simplified version of the game's state for the local client perspective, used
 * to handle some functionalities in an easier way.
 */
public class ClientGame {
    private final LocalPlayer localPlayer;
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
        this.localPlayer.setViewReferences(gameView,eventSubmitter);
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
        this.remotePlayers = new ArrayList<>();
        this.commonObjectives = new ArrayList<>();
        this.playerWithTurn = null;
    }

    /**
     * Setter for the drawable options attribute.
     * @param drawableOptions a map containing all the drawable cards.
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions));
    }

    /**
     * Method used to enable the draw phase for the player.
     * @param drawableOptions the cards the player can draw from.
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
                put(entry.getKey(), newValue);
            }
        }};
    }

    /**
     * @return the local player.
     */
    public LocalPlayer getLocalPlayer(){
        return this.localPlayer;
    }

    /**
     * @return a list of the remote players (all the players in the same game other than the local player).
     */
    public List<RemotePlayer> getRemotePlayers(){
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    /**
     * @return a map that contains each player's nickname (key), and its color.
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
     * Method that adds a player to the remote players list.
     */
    public void addRemotePlayer(RemotePlayer player){
        remotePlayers.add(player);
        player.setViewReferences(gameView,eventSubmitter);
        eventSubmitter.submit(() -> gameView.showUserJoined(player.getNickname(), player.getColor()));
    }

    /**
     * Setter for the common objectives attribute.
     * @param commonObjectives a list containing the objectives shared by each player in the game.
     */
    public void setCommonObjectives(List<Objective> commonObjectives) {
        this.commonObjectives = new ArrayList<>(commonObjectives);
        eventSubmitter.submit(() -> gameView.showCommonObjectives(getCommonObjectives()));
    }

    /**
     * Method that updates the turn cycle between players.
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