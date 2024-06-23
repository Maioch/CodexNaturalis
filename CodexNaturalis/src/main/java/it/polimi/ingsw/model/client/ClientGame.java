package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;

import java.util.*;

/**
 * Simplified version of the game model. It saves the main information about the game,
 * such as all the players participating, the objectives, the cards that can be drawn and the active player.
 * This class is needed to handle some MVC functionalities in an easier way.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ClientGame {

    //the game's required number of players.
    private final int numberOfPlayers;

    //the player that's using the client instance.
    private final LocalPlayer localPlayer;

    //the other players connected to the match.
    private final List<RemotePlayer> remotePlayers;

    //the game's common objectives.
    private List<Objective> commonObjectives;

    //the ClientPlayer who's currently playing their turn.
    private ClientPlayer playerWithTurn;

    //the event submitter used to update the view.
    private final EventSubmitter eventSubmitter;

    //the current gameView's reference.
    private final GameView gameView;

    //the game's id.
    private final int gameId;

    /**
     * Class constructor.
     *
     * @param player          the player associated with the client.
     * @param eventSubmitter  the medium used to submit a player action to the server, mainly to update the player's view.
     * @param gameView        the view (CLI/GUI) associated to the player.
     * @param numberOfPlayers the number of players for this game.
     * @param gameId          the id of this game.
     *
     * @see LocalPlayer
     * @see EventSubmitter
     * @see GameView
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
     * Checks if the game is full
     *
     * @return true if it's full, false otherwise
     */
    public boolean isGameFull(){
        return remotePlayers.size() + 1 == numberOfPlayers;
    }

    /**
     * Gets the game's number of players the game needs to be played.
     *
     * @return the number of players.
     */
    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    /**
     * Gets the id of this game.
     *
     * @return the id of this game.
     */
    public int getGameId() {
        return gameId;
    }

    /**
     * Updates the cards that can be drawn by a player as well as updating the view.
     *
     * @param drawableOptions   the cards the player can draw from.
     * @param numberOfCardsLeft the number of card left in each deck.
     *
     * @see CardType
     * @see BasicCard
     */
    public void setDrawableOptions(Map<CardType, List<BasicCard>> drawableOptions, Map<CardType,Integer> numberOfCardsLeft) {
        eventSubmitter.submit(() -> gameView.updateDecks(drawableOptions, numberOfCardsLeft));
    }

    /**
     * Requests the local player to draw a card from the drawable options by updating the view.
     *
     * @param drawableOptions the cards the player can draw from.
     * @param numberOfCardsLeft the number of card left in each deck.
     *
     * @see CardType
     * @see BasicCard
     */
    public void requestDraw(Map<CardType, List<BasicCard>> drawableOptions, Map<CardType, Integer> numberOfCardsLeft) {
        eventSubmitter.submit(() -> gameView.requestDraw(drawableOptions, numberOfCardsLeft));
    }

    /**
     * Gets the player associated to this client.
     *
     * @return the local player.
     *
     * @see LocalPlayer
     */
    public LocalPlayer getLocalPlayer(){
        return this.localPlayer;
    }

    /**
     * Gets the players connected to the same game as the local player.
     *
     * @return the remote players.
     *
     * @see RemotePlayer
     */
    public List<RemotePlayer> getRemotePlayers(){
        return new ArrayList<>(){{
            for(RemotePlayer remotePlayer : remotePlayers){
                add(new RemotePlayer(remotePlayer));
            }
        }};
    }

    /**
     * Gets the player with the specified nickname.
     *
     * @param nickname the player nickname.
     *
     * @return the player with the specified nickname.
     *
     * @see ClientPlayer
     */
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
     * Gets a summary of the colors chosen by the players, local and remote, ordered by turn number.
     *
     * @return each player's nickname and his color.
     *
     * @see Content
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
     * Adds a player to this remote players list and updates the view.
     *
     * @param player the remote player to add.
     *
     * @see RemotePlayer
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
     * Removes the parameter player from the remote player list and updates the view.
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
     * Updates the objectives shared by all the players connected to the game and updates the view.
     *
     * @param commonObjectives the list of common objectives.
     *
     * @see Objective
     */
    public void setCommonObjectives(List<Objective> commonObjectives) {
        this.commonObjectives = new ArrayList<>(commonObjectives);
        eventSubmitter.submit(() -> gameView.showCommonObjectives(getCommonObjectives()));
    }

    /**
     * Gets the active player.
     *
     * @return the player that has the turn.
     *
     * @see ClientPlayer
     */
    public ClientPlayer getPlayerWithTurn(){
        return playerWithTurn;
    }

    /**
     * Updates the active player and, if show is true, updates the view.
     *
     * @param nickname the nickname of the player that has the turn.
     * @param show     flag that determines whether to update the view.
     */
    public void setPlayerWithTurn(String nickname, boolean show) {
        ClientPlayer remotePlayerWithTurn = remotePlayers.stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst()
                .orElse(null);
        playerWithTurn = remotePlayerWithTurn == null ? localPlayer : remotePlayerWithTurn;
        String playerNickname = playerWithTurn.getNickname();
        if (show) {
            eventSubmitter.submit(() -> gameView.turnChanged(playerNickname));
        }
    }

    /**
     * Gets the objectives shared by all the players connected to the game.
     *
     * @return the common objectives.
     *
     * @see Objective
     */
    public List<Objective> getCommonObjectives(){
        return new ArrayList<>(){{
            for(Objective obj : commonObjectives){
                add(new Objective(obj));
            }
        }};
    }
}