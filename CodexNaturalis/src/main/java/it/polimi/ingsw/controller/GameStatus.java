package it.polimi.ingsw.controller;

/**
 * Enum that represents the status of the game: lobby if the game hasn't started yet, started if the game is in progress
 * and player_disconnected if one or more of the player's disconnected for whatever reason.
 */
public enum GameStatus {
    LOBBY("⏳ in lobby"),
    STARTED("\uD83D\uDD12 in progress"),
    PLAYER_DISCONNECTED("\uD83D\uDDF2 player disconnected");
    private final String text;

    GameStatus(String text){
        this.text = text;
    }

    public String getText(){
        return text;
    }
}