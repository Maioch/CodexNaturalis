package it.polimi.ingsw.network.server;

import it.polimi.ingsw.controller.GameController;
import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;

import java.util.HashMap;

public class GamesManager {
    private final HashMap<Integer, GameController> games;

    public GamesManager(){
        this.games = new HashMap<>();
    }

    public synchronized int addGame(int numberOfPlayers, String name) throws IllegalNumberOfPlayers {
        int gameId = (!games.containsKey(1)) ? 0 :
                games.keySet().stream()
                        .filter(x -> games.get(x + 1) == null)
                        .min(Integer::compareTo).orElse(0);
        GameController newController = new GameController(numberOfPlayers, new ServerSubject(), name);
        games.put(gameId + 1, newController);
        return gameId + 1;
    }

    public synchronized GameController getController(int gameId){
        return games.get(gameId);
    }

    public synchronized HashMap<Integer, String> getFormattedMatches(){
        return new HashMap<>(){{
            for(Entry<Integer, GameController> entry : games.entrySet()){
                put(entry.getKey(), entry.getValue().getName());
            }
        }};
    }
}