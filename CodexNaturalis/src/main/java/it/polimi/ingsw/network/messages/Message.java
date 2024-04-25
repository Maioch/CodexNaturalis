package it.polimi.ingsw.network.messages;

/**
 * Enum that represents every possible message. Each message is sent by both the server and the client
 * Each value notifies the receiver about the next message's contents
 * @author Guglielmo Gatti, Andrea Fidanza, Marco Maiocchi
 */
public enum Message {

    /**
     * Client: requests the available matches, Server: the next message will contain the available matches.
     */
    SHOW_MATCHES(""),

    /**
     * Client: requests the creation of a new game.
     */
    NEW_GAME(""),

    /**
     * Client: the next message will contain the chosen number of players, Server: requests the number of players.
     */
    REQUEST_PLAYER_NUMBER(""),

    /**
     * Server: the chosen number of players is valid
     */
    PLAYER_NUMBER_OK(""),

    /**
     * Client: the next message will contain the chosen game.
     */
    JOIN_GAME(""),

    /**
     * Server: the chosen game is not full
     */
    JOIN_GAME_OK(""),

    /**
     * Client: the next message will contain the chosen username, Server: requests the username.
     */
    REQUEST_USERNAME(""),

    /**
     * Server: the chosen username is valid
     */
    USERNAME_OK(""),

    /**
     * Client: the next message will contain the chosen color, Server: requests the color.
     */
    REQUEST_COLOR(""),

    /**
     * Server: the chosen color is valid
     */
    COLOR_OK(""),

    /**
     * Server: the next message will contain the username of the player that joined the game
     */
    NEW_PLAYER_JOINED(""),

    /**
     * Client: the next message will contain the sides of the starter card that the player wants to place
     * Server: the next message will contain the starter card given to the player (front and back).
     */
    GAME_STARTED(""),

    /**
     * Server: the next message will contain the username of the player who is supposed to play the turn.
     */
    TURN_NOTIFICATION(""),

    /**
     * Server: the next message will contain the hand of the player.
     */
    HAND_CARDS(""),

    /**
     * Client: the next message will contain the card chosen by the player along with the corner where
     * it's going to be placed Server: notifies the player that they have to place a card
     */
    PLACE_CARD(""),

    /**
     * Server: notifies every player about the successful placement of a card.
     * the next message will contain the nickname of the player who placed the card, the card that has been placed,
     * and the corner where it has been placed
     */
    PLACEMENT_OK(""),

    /**
     * Server: notifies the player that the placement they supplied is not valid.
     */
    PLACEMENT_FAILED(""),

    /**
     * Server: the next message will contain a representation of the decks' current state
     */
    DRAW_OPTIONS(""),

    /**
     * Client: the next message will contain the index and the deck type from which the user
     * Server: notifies that the player has to draw a card (followed by HAND_CARDS message)
     */
    DRAW(""),

    /**
     * Server: notifies the players that the turn that's about to be played will be their last.
     */
    LAST_TURN(""),

    /**
     * Server: the next message will contain the nickname of the person who won.
     */
    DECLARE_WINNER(""),

    /**
     * Client: a player left the game voluntarily, Server: the next message will contain the nickname of the player who left
     */
    PLAYER_DISCONNECTED("");

    Message(String message){}
}