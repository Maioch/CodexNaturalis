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
 * ClientGame is a simplified version of the game model.
 * It saves the main information about the game, such as all the players participating, the objectives, the cards that
 * can be drawn and the active player.
 * This class is needed to handle some MVC functionalities in an easier way.
 */
public class ClientGame {

    private final int numberOfPlayers;
    private final LocalPlayer localPlayer;
    private final List<RemotePlayer> remotePlayers;
    private List<Objective> commonObjectives;
    private Map<CardType, List<BasicCard>> drawableOptions;
    private ClientPlayer playerWithTurn;
    private final EventSubmitter eventSubmitter;
    private final GameView gameView;

    /**
     * Class constructor.
     *
     * @param player         the player associated with the client.
     * @param eventSubmitter the medium used to submit a player action to the server, mainly to update the player's view.
     * @param gameView       the view (CLI/GUI) associated to the player.
     */
    public ClientGame(LocalPlayer player, EventSubmitter eventSubmitter, GameView gameView, int numberOfPlayers) {
        this.localPlayer = player;
        this.localPlayer.setViewReferences(gameView,eventSubmitter);
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
        this.remotePlayers = new ArrayList<>();
        this.commonObjectives = new ArrayList<>();
        this.playerWithTurn = null;
        this.numberOfPlayers = numberOfPlayers;
    }

    /**
     * Checks if the game is now full
     *
     * @return true if it's full, false ootherewise
     */
    public boolean isGameFull(){
        return remotePlayers.size() + 1 == numberOfPlayers;
    }

    /**
     * Returns the game's number of players the game needs to be played.
     *
     * @param numberOfPlayers the number of players.
     */
    public int getNumberOfPlayers(int numberOfPlayers) {
        return numberOfPlayers;
    }

    /**
     * Updates the cards that can be drawn by a player.
     *
     * @param drawableOptions the cards the player can draw from.
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions));
    }

    /**
     * Updates the player's turn phase, setting it to draw.
     *
     * @param drawableOptions the cards the player can draw from.
     */
    public void requestDraw(Map<CardType, List<BasicCard>> drawableOptions) {
        this.drawableOptions = new HashMap<>(drawableOptions);
        eventSubmitter.submit(() -> gameView.requestDraw(drawableOptions));
    }

    /**
     * Returns the summary of all the cards the player can draw in a given moment.
     * The returned cards are divided by type.
     *
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
     * Returns the player associated to this client.
     *
     * @return the local player.
     */
    public LocalPlayer getLocalPlayer(){
        return this.localPlayer;
    }

    /**
     * Returns the players connected to the same game as the local player.
     *
     * @return the remote players.
     */
    public List<RemotePlayer> getRemotePlayers(){
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    /**
     * Returns a summary of the colors chosen by the players, local and remote.
     *
     * @return each player's nickname and his color.
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
     * Adds a player to this remote players list.
     */
    public void addRemotePlayer(RemotePlayer player){
        remotePlayers.add(player);
        player.setViewReferences(gameView, eventSubmitter);
        eventSubmitter.submit(() -> gameView.showUserJoined(player.getNickname(), player.getColor()));
    }

    public void removeRemotePlayer(String nickname){
        remotePlayers.removeIf(remotePlayer -> remotePlayer.getNickname().equals(nickname));
    }

    /**
     * Updates the objectives shared by all the players connected to the game.
     *
     * @param commonObjectives the list of common objectives.
     */
    public void setCommonObjectives(List<Objective> commonObjectives) {
        this.commonObjectives = new ArrayList<>(commonObjectives);
        eventSubmitter.submit(() -> gameView.showCommonObjectives(getCommonObjectives()));
    }

    /**
     * Updates the active player.
     *
     * @param nickname the nickname of the player that has the turn.
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
     * Returns the objectives shared by all the players connected to the game.
     *
     * @return this common objectives.
     */
    public List<Objective> getCommonObjectives(){
        return new ArrayList<>(){{
            for(Objective obj : commonObjectives){
                add(new Objective(obj));
            }
        }};
    }

    /**
     * Returns the active player.
     *
     * @return the player that has the turn.
     */
    public ClientPlayer getPlayerWithTurn(){
        return playerWithTurn;
    }
}