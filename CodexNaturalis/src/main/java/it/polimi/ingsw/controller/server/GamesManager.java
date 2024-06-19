package it.polimi.ingsw.controller.server;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GamesManager manages all games created by the clients, by saving them with a unique ID.
 */
public class GamesManager{

    private final Map<Integer, GameController> games;

    /**
     * Class constructor.
     */
    public GamesManager(){
        this.games = new HashMap<>();
    }

    /**
     * Adds a new game to this list, assigning it the first available ID.
     *
     * @param numberOfPlayers         the maximum number of players that can join the game.
     * @param name                    the name of the game.
     *
     * @return                        the id assigned to the game.
     *
     * @throws IllegalNumberOfPlayers if the player entered an invalid maximum number of players when creating the game.
     */
    public synchronized int addGame(int numberOfPlayers, String name) throws IllegalNumberOfPlayers {
        int gameId = (!games.containsKey(1)) ? 1 :
                games.keySet().stream()
                        .filter(x -> games.get(x + 1) == null)
                        .min(Integer::compareTo).orElse(0) + 1;
        GameInfo gameInfo = new GameInfo(gameId, name, GameStatus.LOBBY);
        GameController newController = new GameController(numberOfPlayers, new ServerSubject(), gameInfo, this::deleteGame);
        new Thread(newController).start();
        games.put(gameId, newController);
        return gameId;
    }

    /**
     * Removes a game from this list.
     *
     * @param game the game to remove.
     */
    public synchronized void deleteGame(GameController game){
        List<Integer> idsToRemove = games.entrySet().stream()
                .filter(e -> e.getValue() == game)
                .map(Map.Entry::getKey)
                .toList();
        for(Integer id : idsToRemove){
            games.remove(id);
        }
    }

    /**
     * Returns the controller of a given game in this list.
     *
     * @param gameId the game's ID.
     *
     * @return the game's controller.
     */
    public synchronized GameController getController(int gameId){
        return games.get(gameId);
    }

    /**
     * Returns a summary of the games in this list, formatted used the GameInfo record.
     *
     * @return a list that contains each game's id along with its name.
     *
     * @see GameInfo
     */
    public synchronized List<GameInfo> getFormattedAvailableMatches(){
        List<GameInfo> result = new ArrayList<>();
        for(Map.Entry<Integer, GameController> entry : games.entrySet()){
            result.add(new GameInfo(entry.getKey(), entry.getValue().getName(), entry.getValue().getGameStatus()));
        }
        return result;
    }
}