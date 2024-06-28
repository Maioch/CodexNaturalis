package it.polimi.ingsw.controller.server;

/**
 * GameStatus is the state a game is in at a given time.
 * LOBBY: the game hasn't started yet, waiting for players.
 * STARTED: the game is in progress.
 * PLAYER_DISCONNECTED: one or more players disconnected from the game.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public enum GameStatus {

    /**
     * The game is waiting to have enough players to start.
     */
    LOBBY("⏳ lobby"),
    /**
     * the game has started
     */
    STARTED("\uD83D\uDD12 playing"),
    /**
     * the game has started, but one or more players have been disconnected.
     */
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
     * Gets the text of the entry, used to print the correct game's status when using the CLI.
     *
     * @return entry's text.
     */
    public String getText(){
        return text;
    }
}