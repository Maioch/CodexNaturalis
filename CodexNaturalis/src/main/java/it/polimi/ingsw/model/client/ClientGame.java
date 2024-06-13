package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;

import java.util.*;

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
    private ClientPlayer playerWithTurn;
    private final EventSubmitter eventSubmitter;
    private final GameView gameView;
    private final int gameId;

    /**
     * Class constructor.
     *
     * @param player         the player associated with the client.
     * @param eventSubmitter the medium used to submit a player action to the server, mainly to update the player's view.
     * @param gameView       the view (CLI/GUI) associated to the player.
     */
    public ClientGame(LocalPlayer player, EventSubmitter eventSubmitter, GameView gameView, int numberOfPlayers, int gameId) {
        this.localPlayer = player;
        this.localPlayer.setViewReferences(gameView, eventSubmitter);
        this.gameView = gameView;
        this.eventSubmitter = eventSubmitter;
        this.remotePlayers = new ArrayList<>();
        this.commonObjectives = new ArrayList<>();
        this.playerWithTurn = null;
        this.numberOfPlayers = numberOfPlayers;
        this.gameId = gameId;
    }

    /**
     * Checks if the game is now full
     *
     * @return true if it's full, false otherwise
     */
    public boolean isGameFull(){
        return remotePlayers.size() + 1 == numberOfPlayers;
    }

    /**
     * Returns the game's number of players the game needs to be played.
     *
     * @return the number of players.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public int getGameId() { return gameId; }

    /**
     * Updates the cards that can be drawn by a player.
     *
     * @param drawableOptions the cards the player can draw from.
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions, Map<CardType,Integer> numberOfCardsLeft) {
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions, numberOfCardsLeft));
    }

    /**
     * Updates the player's turn phase, setting it to draw.
     *
     * @param drawableOptions the cards the player can draw from.
     */
    public void requestDraw(Map<CardType, List<BasicCard>> drawableOptions, Map<CardType, Integer> numberOfCardsLeft) {
        eventSubmitter.submit(() -> gameView.requestDraw(drawableOptions, numberOfCardsLeft));
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

    public ClientPlayer getPlayerWithNickname(String nickname){
        if(localPlayer.getNickname().equals(nickname)){
            return localPlayer;
        }
        return remotePlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Returns a summary of the colors chosen by the players, local and remote, ordered by turn number.
     *
     * @return each player's nickname and his color.
     */
    public Map<String, Content> getPlayerColors(){
        Map<String, Content> playersColors = new LinkedHashMap<>();
        List<ClientPlayer> players = new ArrayList<>(){{
            add(localPlayer);
            addAll(remotePlayers);
        }};
        players.sort(Comparator.comparing(ClientPlayer::getTurnNumber));
        for(ClientPlayer player : players){
            playersColors.put(player.getNickname(), player.getColor());
        }
        return playersColors;
    }

    /**
     * Adds a player to this remote players list.
     */
    public void addRemotePlayer(RemotePlayer player){
        remotePlayers.add(player);
        player.setViewReferences(gameView, eventSubmitter);
        boolean gameFull = isGameFull();
        String playerNickname = player.getNickname();
        Content playerColor = player.getColor();
        eventSubmitter.submit(() -> gameView.showUserJoined(playerNickname, playerColor, gameFull));
    }

    /**
     * Removes the parameter player from the remote player list.
     *
     * @param nickname the nickname of the player to remove.
     */
    public void removeRemotePlayer(String nickname){
        Content playerColor = getPlayerColors().get(nickname) != null ?
                getPlayerColors().get(nickname) :
                Content.WHITE;
        remotePlayers.removeIf(remotePlayer -> remotePlayer.getNickname().equals(nickname));
        eventSubmitter.submit(() -> gameView.notifyPlayerLeftLobby(nickname, playerColor));
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
     * Returns the active player.
     *
     * @return the player that has the turn.
     */
    public ClientPlayer getPlayerWithTurn(){
        return playerWithTurn;
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
        eventSubmitter.submit(() -> gameView.turnChanged(playerWithTurn.getNickname()));
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
}