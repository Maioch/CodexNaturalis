package it.polimi.ingsw.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.polimi.ingsw.model.shared.card.CardType;

import java.io.IOException;

/**
 * Contains static methods used to retrieve the application parameters from a json file.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class Parameters {

    //the path where all the parameters are stored, relative to the "resources" folder.
    private final static String filePath = "/gameFiles/";

    //the name of the file containing the game's parameters.
    private final static String gameParametersFile = "gameParameters.json";

    //the name of the file containing the client's parameters.
    private final static String clientParametersFile = "clientParameters.json";

    //the name of the file containing the server's parameters.
    private final static String serverParametersFile = "serverParameters.json";

    /**
     * Class constructor.
     */
    public Parameters() {}

    /**
     * Returns the index of the first card of the parameter type.
     * Indexes start from 1 and are sequential for all the game card: first resource cards, then gold ones, starters and
     * finally objectives.
     *
     * @param type the card type.
     *
     * @return     the id of the first card of the specified type.
     *
     * @see CardType
     */
    public static int getStartCardIndex(CardType type){
        return getParameter(gameParametersFile,type.toString().toLowerCase() + "CardStartIndex").asInt();
    }

    /**
     * Returns the index of the last card of the parameter type.
     * Indexes start from 1 and are sequential for all the game card: first resource cards, then gold ones, starters and
     * finally objectives.
     *
     * @param type the card type.
     *
     * @return     the id of the last card of the specified type.
     *
     * @see CardType
     */
    public static int getEndCardIndex(CardType type){
        return getParameter(gameParametersFile,type.toString().toLowerCase() + "CardEndIndex").asInt();
    }

    /**
     * Returns the number of visible cards that can be drawn by both resource and gold decks.
     *
     * @return the number of visible cards.
     */
    public static int getNumberOfVisibleCards(){
        return getParameter(gameParametersFile,"numberOfVisibleCards").asInt();
    }

    /**
     * Returns the number of gold cards each player has in his hand at the start of the game.
     *
     * @return the number of gold hand cards.
     */
    public static int getNumberOfGoldCardsInHand(){
        return getParameter(gameParametersFile,"numberOfGoldCardsInHand").asInt();
    }

    /**
     * Returns the number of resource cards each player has in his hand at the start of the game.
     *
     * @return the number of resource hand cards.
     */
    public static int getNumberOfResourceCardsInHand(){
        return getParameter(gameParametersFile,"numberOfResourceCardsInHand").asInt();
    }

    /**
     * Returns the number of secret objectives each player has.
     *
     * @return the number of secret objectives.
     */
    public static int getNumberOfSecretObjectives(){
        return getParameter(gameParametersFile,"numberOfSecretObjectives").asInt();
    }

    /**
     * Returns the number of objectives drawn at the start of the game, from which a player has to choose.
     *
     * @return the number of secret objectives which the player has to choose from.
     */
    public static int getNumberOfDrawnSecretObjectives() {
        return getParameter(gameParametersFile,"numberOfDrawnSecretObjectives").asInt();
    }

    /**
     * Returns the time needed for the game to end by forfeit if only one player is connected.
     *
     * @return the time elapsed before the game ends by forfeit.
     */
    public static int getForfeitTime() {
        return getParameter(serverParametersFile,"forfeitTime").asInt();
    }

    /**
     * Returns the number of objectives common to all players.
     *
     * @return the number of common objectives.
     */
    public static int getNumberOfCommonObjectives(){
        return getParameter(gameParametersFile,"numberOfCommonObjectives").asInt();
    }

    /**
     * Returns the maximum number of players that can take part of a game.
     *
     * @return the maximum number of players that can take part of a game.
     */
    public static int getMaxPlayers(){
        return getParameter(gameParametersFile,"maxNumberOfPlayers").asInt();
    }

    /**
     * Returns the minimum number of players needed to start a game.
     *
     * @return the minimum number of players needed to start a game.
     */
    public static int getMinPlayers(){
        return getParameter(gameParametersFile,"minNumberOfPlayers").asInt();
    }

    /**
     * Returns the points threshold at which the game enters its final phase.
     *
     * @return the points threshold required to trigger the last turn.
     */
    public static int getWinThreshold(){
        return getParameter(gameParametersFile,"winThreshold").asInt();
    }

    /**
     * Returns the port that the player has to enter to use the TCP technology to play the game.
     *
     * @return the port associated to the TCP connections.
     */
    public static int getTCPPort() {
        return getParameter(serverParametersFile,"tcpPort").asInt();
    }

    /**
     * Returns the port that the player has to enter to use the RMI technology to play the game.
     *
     * @return the port associated to the RMI connections.
     */
    public static int getRMIPort() {
        return getParameter(serverParametersFile,"rmiPort").asInt();
    }

    /**
     * Returns the maximum number of characters a name can contain (player name or game name).
     *
     * @return the maximum length of a name.
     */
    public static int getMaxNameLength() {
        return getParameter(serverParametersFile,"maxNameLength").asInt();
    }

    /**
     * Returns the maximum number of characters a chat message can contain.
     *
     * @return the maximum length of a chat message.
     */
    public static int getMaxChatMessageLength() {
        return getParameter(serverParametersFile,"maxChatMessageLength").asInt();
    }

    /**
     * Returns the number of seconds of delay between a ping and the next one (server side).
     *
     * @return the pinging period.
     */
    public static int getServerPingPeriodSeconds(){
        return getParameter(serverParametersFile,"pingPeriodSeconds").asInt();
    }

    /**
     * Returns the number of seconds of delay between a ping and the next one (client side).
     *
     * @return the pinging period.
     */
    public static int getClientPingPeriodSeconds(){
        return getParameter(clientParametersFile,"pingPeriodSeconds").asInt();
    }

    /**
     * Returns the maximum number of seconds a lobby can stay empty.
     *
     * @return the time elapsed before a lobby gets removed if is empty.
     */
    public static int getLobbyTimeout(){
        return getParameter(serverParametersFile,"lobbyTimeout").asInt();
    }

    /**
     * Returns the server logger name.
     *
     * @return the name of the server logger.
     */
    public static String getLoggerName(){
        return getParameter(serverParametersFile, "loggerName").asText();
    }

    /**
     * Returns the special character used for commands in the game's CLI.
     *
     * @return the prefix used to trigger a command using the CLI version of the game.
     */
    public static String getCommandChar() {
        return getParameter(clientParametersFile, "commandChar").asText();
    }

    /**
     * Returns the special character used to separate players' nicknames in chat commands in the game's CLI.
     *
     * @return the char used to separate players' nicknames arguments of a chat command.
     */
    public static String getDelimiter() {
        return getParameter(clientParametersFile, "delimiter").asText();
    }

    /**
     * Returns the reply given by the CLI to the player that uses the HELP command during the match.
     *
     * @return the body of the "/HELP" command during the game phase.
     */
    public static String getGameHelpBody(){
        return getParameter(clientParametersFile, "gameHelpBody").asText();
    }

    /**
     * Returns the reply given by the CLI to the player that uses the HELP command in the lobby.
     *
     * @return the body of the "/HELP" command during the setup phase.
     */
    public static String getSetupHelpBody() {
        return getParameter(clientParametersFile, "setupHelpBody").asText();
    }

    /**
     * Returns the URL of the rules of the game.
     *
     * @return the URL to which players are redirected when they use the "/GETRULES" command.
     */
    public static String getRulesURL() {
        return getParameter(clientParametersFile, "rulesURL").asText();
    }

    /**
     * Returns the formatted title of the game for the CLI mode.
     *
     * @return the title of the game to print on terminal
     */
    public static String getTitle(){
        return getParameter(clientParametersFile, "gameTitle").asText();
    }

    /**
     * Gets the specified parameter read on the json file.
     *
     * @param parameter the parameter to retrieve from the file.
     *
     * @return          the node corresponding to the given parameter.
     */
    private static JsonNode getParameter(String fileName, String parameter){
        @SuppressWarnings("DuplicatedCode")
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(Parameters.class.getResource(filePath + fileName)).get(parameter);
            return node;
        }
        catch(IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }
}