package it.polimi.ingsw.network.shared.messages;

/**
 * Represents every possible message. Each message is sent by both the server and the client.
 *
 * @author Guglielmo Gatti, Andrea Fidanza, Marco Maiocchi
 */
public enum Status {
    /**
     * Client: requests the available matches.
     * Server: sends the available matches.
     */
    REQUEST_GAMES(""),

    /**
     * Client: requests the creation of a new game sending the desired number of players and the game name.
     * Server: sends the game id.
     */
    NEW_GAME(""),

    /**
     * Server: notifies that the game creation failed (caused by an invalid number of players choice).
     */
    INVALID_PLAYERS_NUMBER("Invalid number of players. Please enter a valid one."),

    /**
     * Client: requests the available color, sending the game id.
     * Server: sends the available colors and the game id.
     */
    REQUEST_COLORS(""),

    /**
     * Client: requests to join an existing game, along with the game's id, its chosen color and nickname.
     * This message is sent also when the game was "created" by the client itself.
     * Server: sends the nickname and the color of the player.
     */
    JOIN_GAME(""),

    /**
     * Server: restarts the game joining process from the start (REQUEST_GAMES).
     */
    GAME_FULL("The game can't be joined because it is full."),

    /**
     * Server: requests a new username, as the one chosen before was already taken;
     * contains the game id of the match that the player is trying to join.
     */
    INVALID_NICKNAME("The chosen username is already taken. Please enter a different one."),

    /**
     * Server: requests a new color (and sends the game id), as the one chosen before was already taken;
     * contains the game id of the match that the player is trying to join.
     */
    INVALID_COLOR("The chosen color is already taken. Please enter a different one."),

    /**
     * Server: notifies every player that a new one joined the match, sending its color and nickname.
     */
    NEW_PLAYER_JOINED(""),

    /**
     * Server: sends all the available draw options.
     */
    DRAW_OPTIONS(""),

    /**
     * Server: sends to every player the username of the player who is supposed to play the turn.
     */
    TURN_NOTIFICATION(""),

    /**
     * Server: sends to every player the username of the player who is supposed to play the turn without updating the view.
     * Used in reconnection.
     */
    SILENT_TURN_NOTIFICATION(""),

    /**
     * Server: sends to the client the player's hand cards.
     * Client: sends the side of the starter card that the player wants to place.
     */
    STARTER_CARD(""),

    /**
     * Server: notifies the player that the starter card placement they supplied is not valid.
     */
    INVALID_STARTER_CARD("The chosen starter card is invalid. Please select a different one."),

    /**
     * Server: notifies every player about the successful placement of a card and sends them the updated board and score.
     */
    PLACEMENT_OK(""),

    /**
     * Server: sends to the player his hand cards.
     */
    PLAYER_HAND_CARDS(""),

    /**
     * Server: sends to every player his hand cards back side.
     */
    PLAYER_HAND_BACK(""),

    /**
     * Server: sends the common objectives.
     */
    COMMON_OBJECTIVES(""),

    /**
     * Server: asks the client to choose a set amount of objectives from a list
     * Client: sends the chosen objectives
     */
    REQUEST_SECRET_OBJECTIVES(""),

    /**
     * Server: asks the client to choose a set amount of objectives from a list, if the first response wasn't valid.
     */
    INVALID_SECRET_OBJECTIVES(""),

    /**
     * Server: sends to the player his objectives (both common and personal).
     */
    SECRET_OBJECTIVES(""),

    /**
     * Server: requests the player to place a card, sending each placeable card and corner.
     * Client: sends to the server the chosen card and corner.
     */
    PLACE_CARD(""),

    /**
     * Server: notifies the player that the placement they supplied is not valid.
     */
    INVALID_PLACE_CARD("The chosen card is invalid. Please select a different one."),

    /**
     * Server: notifies that the player has to draw a card and sends him all the available options.
     * Client: sends the index and the deck type from which the user will draw.
     */
    DRAW(""),

    /**
     * Server: notifies the player that the draw they submitted is not valid and sends him the available options.
     */
    INVALID_DRAW(""),

    /**
     * Server: notifies every player that the turn that's about to be played will be their last.
     */
    LAST_TURN(""),

    /**
     * Server: sends to every player his final score.
     */
    PLAYER_FINAL_SCORE(""),

    /**
     * Server: sends the nickname of the person who won.
     */
    DECLARE_WINNER(""),

    /**
     * Client: a player left the game voluntarily, Server: the next message will contain the nickname of the player who left.
     */
    PLAYER_DISCONNECTED(""),

    /**
     * Server: the next message will contain the nickname of the player who left. Won't show any notification on the view.
     */
    QUIET_PLAYER_DISCONNECTED(""),

    /**
     * Server: a player left the lobby
     */
    PLAYER_LEFT_LOBBY(""),

    /**
     * Client: the player tries to rejoin the game
     */
    RECONNECT(""),

    /**
     * Server: the player tried to reconnect to a game that doesn't exist anymore.
     */
    INVALID_RECONNECT("Couldn't reconnect to the previous match. Returning to lobby..."),

    /**
     * Client: sends a chat message (which contains the sender, recipient and, naturally, the content of the message itself).
     */
    CHAT(""),

    /**
     * Message sent when an error occurs.
     */
    ERROR(""),

    /**
     * message sent whenever a user tries to reconnect to a game using a nickname that doesn't
     * belong to a disconnected player
     */
    WRONG_NAME("the nickname you entered doesn't belong to any of the disconnected players."),

    /**
     * Message sent if a player has no moves.
     */
    NO_MOVES(""),

    /**
     * Message sent if a player's turn has been skipped due to the player being disconnected.
     */
    TURN_SKIPPED(""),

    /**
     * Message sent every interval to all the players in a match
     * in order to check whether they're still connected.
     */
    REQUEST_PING(""),

    /**
     * Message sent in response to a ping request
     */
    PING_ACK(""),

    /**
     * Message sent by the server whenever the timeout that declares the only player left as winner starts.
     */
    GAME_TIMEOUT_STARTED(""),

    /**
     * Message by the server sent whenever the match lobby's timeout ends.
     */
    GAME_CANCELED("");

    private final String message;

    /**
     * Enum constructor.
     *
     * @param message the message associated with the status.
     */
    Status(String message){
        this.message = message;
    }

    /**
     * Gets the status message.
     *
     * @return the status message.
     */
    public String getMessage(){
        return message;
    }
}