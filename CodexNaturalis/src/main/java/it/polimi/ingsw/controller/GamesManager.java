package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.server.ServerSubject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that manages all the existing games created by the clients; it contains each game controller along with a unique
 * integer id.
 */
public class GamesManager{
    private final Map<Integer, GameController> games;

    /**
     * Constructor for the class.
     */
    public GamesManager(){
        this.games = new HashMap<>();
    }

    /**
     * Method that adds a new game to the list of existing games, checking which is the lowest available index to assign to it.
     * @param numberOfPlayers the maximum number of players that can join the game.
     * @param name the name of the game.
     * @return the id associated with the newly created game.
     * @throws IllegalNumberOfPlayers exception thrown if the player entered an invalid players number parameter.
     */
    public synchronized int addGame(int numberOfPlayers, String name) throws IllegalNumberOfPlayers {
        int gameId = (!games.containsKey(1)) ? 0 :
                games.keySet().stream()
                        .filter(x -> games.get(x + 1) == null)
                        .min(Integer::compareTo).orElse(0);
        GameController newController = new GameController(numberOfPlayers, new ServerSubject(), name, this::deleteGame);
        new Thread(newController).start();
        games.put(gameId + 1, newController);
        return gameId + 1;
    }

    /**
     * Method that deletes one of the games present in the list.
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
     * @param gameId the id associated with an existing controller.
     * @return the controller associated with the given id
     */
    public synchronized GameController getController(int gameId){
        return games.get(gameId);
    }

    /**
     * @return a map that contains each game's id along with its name.
     */
    public synchronized Map<Integer, String> getFormattedAvailableMatches(){
        return new HashMap<>(){{
            for(Entry<Integer, GameController> entry : games.entrySet()){
                if(!entry.getValue().isGameFull()){
                    put(entry.getKey(), entry.getValue().getName());
                }
            }
        }};
    }
}