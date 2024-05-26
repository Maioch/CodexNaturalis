package it.polimi.ingsw.controller;

/**
 * GameStatus is the state a game is in at a given time.
 * LOBBY: the game hasn't started yet, waiting for players.
 * STARTED: the game is in progress.
 * PLAYER_DISCONNECTED: one or more players disconnected from the game.
 */
public enum GameStatus {

    LOBBY("⏳ lobby"),
    STARTED("\uD83D\uDD12 playing"),
    PLAYER_DISCONNECTED("\uD83D\uDDF2 user left");

    private final String text;

    /**
     * Enum constructor.
     *
     * @param text the sentence associated to each enum entry.
     */
    GameStatus(String text){
        this.text = text;
    }

    /**
     * Returns the text of the entry, used to print the correct game's status when using the CLI.
     *
     * @return entry's text.
     */
    public String getText(){
        return text;
    }
}