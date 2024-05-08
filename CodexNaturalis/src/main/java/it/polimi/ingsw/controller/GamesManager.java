package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.IllegalNumberOfPlayers;
import it.polimi.ingsw.network.server.ServerSubject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamesManager{
    private final Map<Integer, GameController> games;

    public GamesManager(){
        this.games = new HashMap<>();
    }

    public synchronized int addGame(int numberOfPlayers, String name) throws IllegalNumberOfPlayers {
        int gameId = (!games.containsKey(1)) ? 0 :
                games.keySet().stream()
                        .filter(x -> games.get(x + 1) == null)
                        .min(Integer::compareTo).orElse(0);
        GameController newController = new GameController(numberOfPlayers, new ServerSubject(), name, this::deleteGame);
        games.put(gameId + 1, newController);
        return gameId + 1;
    }

    public synchronized void deleteGame(GameController game){
        List<Integer> idsToRemove = games.entrySet().stream()
                .filter(e -> e.getValue() == game)
                .map(Map.Entry::getKey)
                .toList();
        for(Integer id : idsToRemove){
            games.remove(id);
        }
    }

    public synchronized GameController getController(int gameId){
        return games.get(gameId);
    }

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