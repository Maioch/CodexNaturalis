package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.model.shared.card.corner.Location;
import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.CardPlacementMessage;
import it.polimi.ingsw.network.shared.messages.game.ChatMessage;
import it.polimi.ingsw.network.shared.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.network.shared.messages.game.ObjectivesMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.view.GameView;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;

/**
 * Handles the gameplay phase of the game, when the client chooses the CLI version.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
@SuppressWarnings("FieldCanBeLocal")
public class GameCLI extends AbstractCLI implements GameView {

    //the thread used to run readInput whenever it has to be interruptible by certain events.
    private Thread readInputThread;

    //the client instance used for the entire program's lifecycle
    private final Client client;

    //holds the chat messages whenever they can't be shown yet due to the user playing their turn.
    private final Queue<ChatMessage> chatMessageQueue;

    //the threshold after which the view is not going to specify how many cards are left when printing the deck.
    private final int numberOfDeckCardThreshold = 4;

    /**
     * Class constructor.
     *
     * @param client the client that is using the CLI.
     */
    public GameCLI(Client client) {
        this.client = client;
        chatMessageQueue = new LinkedList<>();
    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft){
        System.out.println();
        System.out.println("DRAW PHASE");
        System.out.println("\nHere are the cards you can draw: ");
        int i = 1;
        for(CardType cardType : CardType.values()){
            List<BasicCard> deckCardList = drawableCards.get(cardType);
            if(deckCardList != null && !deckCardList.isEmpty()) {
                System.out.print("\nDeck " + i);
                if(numberOfCardsLeft.get(cardType) >= numberOfDeckCardThreshold){
                    System.out.printf(" (more than %d cards left in this deck)\n", numberOfDeckCardThreshold);
                }
                else{
                    System.out.printf(" (just %d card(s) left in this deck)\n", numberOfCardsLeft.get(cardType));
                }
                String cardOnTop = CardFormatter.getCardString(deckCardList.getFirst());
                System.out.print(cardOnTop.isEmpty() ? "\n   The deck is empty.\n" : cardOnTop);
                System.out.print(CardFormatter.getCardsInfoString(deckCardList.subList(1,deckCardList.size())));
            }
            i++;
        }
        System.out.println();
        List<Integer> choice = readFromInput(
                "Choose which card you want by entering its coordinates separated by a space (starting from 1): ",
                (list -> list.size() == 2 &&
                        list.getFirst() >= 1 && list.getFirst() <= 2 &&
                        list.getLast() >= 1 && list.getLast() <= Parameters.getNumberOfVisibleCards() + 1),
                this::stringToListInt,
                true);
        client.getController().sendMessage(new DrawChoiceMessage(choice.getLast() - 1, CardType.values()[choice.getFirst() - 1]));
    }

    /**
     * Prints a chat message.
     * If some messages are received during the turn making, they will all be printed after it's finished.
     *
     * @param chatMessage contains the sender, the recipients and the message.
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage){
        if(client.getController().getPlayerWithTurn().equals(client.getController().getLocalPlayerName())){
            chatMessageQueue.add(chatMessage);
        }else{
            printChatMessage(chatMessage.getMessage(), chatMessage.getSender(), chatMessage.getRecipients());
        }
    }

    /**
     * Builds the default message string, used by the method above to print a message.
     *
     * @param message the client's message
     * @param sender the sender of the message.
     * @param recipients the recipients of the message.
     */
    private void printChatMessage(String message, String sender, List<String> recipients){
        StringBuilder sb = new StringBuilder();
        Map<String, Content> playerColors = client.getController().getPlayerColors();
        sb.append(playerColors.get(sender).getTextColorString())
                .append(sender)
                .append(Content.EMPTY.getTextColorString());
        if(recipients.size() != client.getController().getRemotePlayerNames().size()) {
            sb.append(" says to ");
            for (String recipient : recipients) {
                sb.append(playerColors.get(sender).getTextColorString())
                        .append(recipient)
                        .append(Content.EMPTY.getTextColorString());
                if(!recipient.equals(recipients.getLast())){
                    sb.append(", ");
                }
            }
        }
        System.out.println(sb.append(": ").append(message));
    }

    /**
     * Requests the client to place a card.
     *
     * @param handCards the client's hand cards.
     * @param placedCards the client's placed cards.
     */
    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards){
        List<BasicCard> validCards = client.getController().getLocalPlayerValidCards();
        List<Corner> validCorners = client.getController().getLocalPlayerValidCorners();
        System.out.println("\nPLACEMENT PHASE");
        System.out.println("\nThese are the cards in your hand: ");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("\nBack side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("Here are the ones you can place: ");
        int numberOfBackSides = Parameters.getNumberOfResourceCardsInHand() + Parameters.getNumberOfGoldCardsInHand();
        int maxCardsInHand = (numberOfBackSides) * 2;
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .limit( numberOfBackSides - maxCardsInHand + validCards.size())
                .toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .skip(numberOfBackSides - maxCardsInHand + validCards.size())
                .toList()));
        int cardIndex = readFromInput("Choose which card you want to place by writing its index (starting from 1): ",
                (i -> i >= 1 && i <= validCards.size()),
                this::stringToInt,
                true) - 1;
        System.out.println("These are the cards placed on your board: ");
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.println("Here are the coordinates of all the corners where you can place the card: ");
        List<Point> validPositions = validCorners.stream().map(c -> new Point(c.getX(), c.getY())).toList();
        for(int i = 0; i < validPositions.size(); i++){
            System.out.printf("   %d. (%.0f, %.0f)\n", i + 1, validPositions.get(i).getX(),validPositions.get(i).getY());
        }
        System.out.println();
        int cornerIndex = readFromInput("Choose the coordinates you prefer by typing their index: ",
                (i -> i >= 1 && i <= validPositions.size()),
                this::stringToInt,
                true) - 1;
        client.getController().sendMessage(new CardPlacementMessage(validCards.get(cardIndex), validCorners.get(cornerIndex)));
    }

    /**
     * Notifies the client about a turn change.
     *
     * @param turnOwner the client that is playing its turn.
     */
    @Override
    public void turnChanged(String turnOwner){
        while(!chatMessageQueue.isEmpty()){
            ChatMessage chatMessage = chatMessageQueue.poll();
            printChatMessage(chatMessage.getMessage(),chatMessage.getSender(),chatMessage.getRecipients());
        }
        Map<String, Content> playerColors = client.getController().getPlayerColors();
        String coloredTurnOwner =  playerColors.get(turnOwner).getTextColorString() + turnOwner + Content.EMPTY.getTextColorString();
        if(!turnOwner.equals(client.getController().getLocalPlayerName())) {
            System.out.printf("\n%s is playing their turn...\n", coloredTurnOwner);
            if(readInputThread == null || !readInputThread.isAlive()) {
                readInputThread = new Thread(() -> readFromInput("", ((c) -> false), this::stringIdentity, true));
                readInputThread.start();
            }
            return;
        }
        System.out.printf("\nIt's your turn, %s!\n", coloredTurnOwner);
        if (readInputThread != null) {
            readInputThread.interrupt();
        }
    }

    /**
     * Prints an error message.
     *
     * @param message the message to print.
     */
    @Override
    public void showErrorMessage(String message){
        System.out.println(message);
    }

    /**
     * Notifies the client about a new player that joined its game.
     *
     * @param player     the new player's nickname.
     * @param color      the new player's color.
     * @param isGameFull the boolean flagging if the game is full or not.
     */
    @Override
    public void showUserJoined(String player, Content color, boolean isGameFull){
        if (!player.equals(client.getController().getLocalPlayerName())) {
            System.out.println("   " +  color.getTextColorString() + player + Content.EMPTY.getTextColorString() + " joined your game!");
        }
    }

    /**
     * Method not implemented for the TUI to prevent message flooding.
     *
     * @param nickname //
     * @param handCards //
     */
    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards){
    }

    /**
     * Updates the client about its current hand.
     *
     * @param handCards the player's hand cards.
     */
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards){
        System.out.println("\n\nThese are the cards in your hand: ");
        System.out.print("\nFront side:\n" + CardFormatter.getCardsInfoString(
                handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("\nBack side:\n" + CardFormatter.getCardsInfoString(
                handCards.stream().map(CardSides::backSide).toList()));
    }

    /**
     * Requests the starter card's placement at the start of the match.
     *
     * @param playerCards the list of cards owned by the player.
     */
    @Override
    public void requestStarterSide(List<CardSides> playerCards){
        List<CardSides> handCards = playerCards.stream().skip(1).toList();
        CardSides starterCard = playerCards.getFirst();
        System.out.printf("\nThese are the %d cards in your hand: \n",
                Parameters.getNumberOfResourceCardsInHand()
                        + Parameters.getNumberOfGoldCardsInHand());
        System.out.print("\nFront side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("\nBack side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("\nAnd this is your starter card: ");
        System.out.print("\nFront side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.frontSide())));
        System.out.println("\nBack side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.backSide())));
        int chosenSide = readFromInput("Enter 1 if you want to place the front side, 2 otherwise: ",
                n -> n >= 1 && n <= 2,
                this::stringToInt,
                true);
        BasicCard chosenStarter = chosenSide == 1 ? starterCard.frontSide() : starterCard.backSide();
        client.getController().sendMessage(new CardPlacementMessage(chosenStarter, null));
    }

    /**
     * Updates the client about another player's board and score.
     * The board is automatically printed when the player finished their turn, but can be printed
     * also by launching a /BOARD command.
     *
     * @param nickname    the player's nickname.
     * @param placedCards the players board.
     * @param score       the player's score.
     */
    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int score){
        System.out.printf("\nHere are %s's current placed cards (centered on his last placed card): \n",
                client.getController().getPlayerColors().get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        int x = placedCards.isEmpty() ? 0 : placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.isEmpty() ? 0 : placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.printf("Their updated score is: %d\n", score);
    }

    /**
     * Requests the personal objective choice.
     *
     * @param objectives the objectives to choose from.
     */
    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        int numberOfSecretObjectives = Parameters.getNumberOfSecretObjectives();
        System.out.printf("\nHere are the available secret objectives (you can choose %d):\n", numberOfSecretObjectives);
        for(int i = 0; i < objectives.size(); i++) {
            System.out.printf("   %d. %s\n", i + 1, CardFormatter.getObjectiveInfoString(objectives.get(i)));
        }
        List<Integer> chosenObjective = readFromInput("Enter the ID of your chosen objective: ",
                l -> l.stream().allMatch(i -> i >= 1 && i <= objectives.size()) && l.size() == numberOfSecretObjectives,
                this::stringToListInt,
                true);
        client.getController().sendMessage(new ObjectivesMessage(Status.REQUEST_SECRET_OBJECTIVES,
                chosenObjective.stream().map(i -> objectives.get(i - 1)).toList()));
    }

    /**
     * Shows the client its personal objectives.
     *
     * @param objectives the player's personal objectives.
     */
    @Override
    public void showPersonalObjectives(List<Objective> objectives){
        System.out.println("\nThese are your personal objectives: ");
        for(Objective objective : objectives){
            System.out.println("   • " + CardFormatter.getObjectiveInfoString(objective));
        }
    }

    /**
     * Shows the client the common objectives.
     *
     * @param objectives the common objectives.
     */
    @Override
    public void showCommonObjectives(List<Objective> objectives){
        System.out.println("\nThese are the objectives shared by you and the other players: ");
        for(Objective objective : objectives){
            System.out.print("   • " + CardFormatter.getObjectiveInfoString(objective));
            System.out.println();
        }
    }

    /**
     * Method not implemented for the TUI to prevent message flooding.
     *
     * @param drawableCards     //
     * @param numberOfCardsLeft //
     */
    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType,Integer> numberOfCardsLeft){}

    /**
     * Notifies the players that it's time for the last game's turn.
     */
    @Override
    public void notifyLastTurn(){
        System.out.println("\nThe next turn will be the last.");
    }

    /**
     * Notifies the players that the turn has been skipped.
     */
    @Override
    public void notifyTurnSkipped() {
        System.out.println("\nThe turn has been skipped because the player isn't connected.");
    }

    /**
     * Notifies that there's just one player in game, and so that an end timeout started.
     */
    @Override
    public void notifyGameTimeout(){
        System.out.printf(
                "You're the only player left. If no players reconnect in the next %d seconds, you'll win by forfeit.\n",
                Parameters.getForfeitTime());
    }

    /**
     * Shows the local player a legend of the symbols printed in the CLI.
     */
    private void showSymbols(){
        for(Content content : Content.values()){
            if(content != Content.EMPTY){
                System.out.printf("%s : %s\n", content.getSymbol(), content.name());
            }
        }
    }

    /**
     * Shows to the local player all the objectives, both common and personal.
     */
    private void showObjectives(){
        showCommonObjectives(client.getController().getCommonObjectives());
        showPersonalObjectives(client.getController().getPersonalObjectives());
    }

    /**
     * Shows to the local player a player's game recap.
     *
     * @param nickname          the player's nickname.
     * @param objectivePoints   a map containing all the player's objectives and related scores.
     * @param finalScore        the player's final score.
     */
    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore){
        System.out.printf("\nHere's a recap of %s's match:\n",
                client.getController().getPlayerColors().get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        for(Map.Entry<Objective, Integer> entry : objectivePoints.entrySet()){
            System.out.print(CardFormatter.getObjectiveInfoString(entry.getKey()));
            System.out.println("   • points gained through this objective: " + entry.getValue());
        }
        System.out.println("   Final score: " + finalScore);
    }

    /**
     * Shows game's winners to the local player; it also enables the client to return to the main menu.
     *
     * @param winners the list of winners.
     */
    @Override
    public void revealWinners(List<String> winners){
        if (readInputThread != null) {
            readInputThread.interrupt();
        }
        System.out.print("\nThe game is over! The winner" + ((winners.size() > 1) ? "s are" : " is "));
        for(String nickname : winners){
            System.out.printf("%s%s",
                    client.getController().getPlayerColors().get(nickname) + nickname + Content.EMPTY.getTextColorString(),
                    (winners.getLast().equals(nickname)) ? ".\n" : ", ");
        }
        readFromInput("Type BACK when you're ready to return to the main menu: ",
                s -> s.equalsIgnoreCase("back"),
                this::stringIdentity,
                false);
        client.getController().backToSetup();
        client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Notifies that a player has disconnected.
     *
     * @param nickname the disconnected player's nickname.
     * @param color    the disconnected player's color.
     * @param quiet    flag that determines whether to update the status label (true for update).
     */
    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color, boolean quiet) {
        if(!quiet) {
            System.out.printf("\n%s disconnected from the game...",
                    color.getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        }
    }

    /**
     * Notifies that a player has left the lobby.
     *
     * @param nickname the player's nickname.
     * @param color the player's color.
     */
    @Override
    public void notifyPlayerLeftLobby(String nickname, Content color) {
        notifyRemotePlayerDisconnected(nickname, color, true);
    }

    /**
     * Notifies that a player has reconnected.
     *
     * @param nickname the player's nickname.
     */
    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        System.out.printf("\n%s is back!\n",
                client.getController().getPlayerColors().get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
    }

    /**
     * Notifies that the game has been cancelled.
     */
    @Override
    public void notifyGameCanceled(){
        System.out.println("The game took too long to start, and it has been canceled.");
        client.getController().backToSetup();
        client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Handles all the possible client's commands.
     *
     * @param command the command to handle.
     * @param argument the arguments associated to the command.
     */
    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "HELP" -> System.out.println(Parameters.getGameHelpBody());
            case "CHAT" -> sendChatMessage(argument);
            case "BOARD" -> showBoard(argument);
            case "HAND" -> showHandCards(argument);
            case "SYMBOLS" -> showSymbols();
            case "OBJECTIVES" -> showObjectives();
            default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
        }
    }

    /**
     * Notifies the client that someone is out of moves.
     *
     * @param nickname the nickname of the stuck player.
     */
    @Override
    public void showNoMovesAvailable(String nickname) {
        Map<String, Content> playerColors = client.getController().getPlayerColors();
        String localPlayerName = client.getController().getLocalPlayerName();
        if(nickname.equals(localPlayerName)){
            System.out.printf("%s, you can no longer make any more moves.\n",
                    playerColors.get(localPlayerName).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        }else{
            System.out.printf("%s cannot make any more moves.\n",
                    playerColors.get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        }
    }

    /**
     * Shows that the local player has suddenly disconnected.
     */
    @Override
    public void showDisconnectionMessage(){
        if (readInputThread != null) {
            readInputThread.interrupt();
        }
        disconnectionProcedure();
    }

    /**
     * Method that computes the chat input, in order to send it.
     * The chat terminal convention is the following: "/CHAT nick1/nick2/... messageInput"
     * The "nicks", are the nicknames of the players to which the message will be sent.
     * If the nicknames aren't specified, the message will be sent to all the players.
     * The nicks MUST be interspersed by "/" slashes.
     * 
     * @param arguments the entire string input that comes after "/CHAT" label-command.
     */
    @SuppressWarnings("SlowListContainsAll")
    private void sendChatMessage(String arguments){
        if(client.getController().getPlayerWithTurn().equals(client.getController().getLocalPlayerName())){
            System.out.println("You cannot send chat messages while your turn is in progress.");
            return;
        }
        List<String> recipients = extractRecipients(arguments);
        String[] splitArgs = arguments.split(" ",2);
        String chatMessage = !recipients.isEmpty() ? splitArgs[1] : arguments;
        if(recipients.isEmpty()){
            recipients = client.getController().getRemotePlayerNames();
        }
        if(client.getController().getRemotePlayerNames().containsAll(recipients)){
            client.getController().sendMessage(new ChatMessage(chatMessage,null, recipients));
        }else{
            System.out.println("Some of the recipients couldn't be found. The message wasn't sent.");
        }
    }

    /**
     * Extracts the recipients of a written chat command.
     *
     * @param arguments the arguments string.
     *
     * @return the list of recipients nicknames.
     */
    private List<String> extractRecipients(String arguments){
        List<String> recipients = new ArrayList<>();
        int indexOfDelimiter = 0;
        while(indexOfDelimiter != -1){
            indexOfDelimiter = arguments.indexOf(Parameters.getDelimiter(),indexOfDelimiter);
            if(indexOfDelimiter != -1){
                int indexOfNextDelimiter = arguments.indexOf(Parameters.getDelimiter(),indexOfDelimiter + 1);
                if(indexOfNextDelimiter != -1){
                    recipients.add(arguments.substring(indexOfDelimiter + 1,indexOfNextDelimiter));
                }
                indexOfDelimiter = indexOfNextDelimiter;
            }
        }
        return recipients;
    }

    /**
     * Shows the board as specified in the written command.
     *
     * @param arguments the arguments string.
     */
    private void showBoard(String arguments){
        String[] splitArgs = arguments.split(" ");
        switch(splitArgs.length){
            case 0,2 -> System.out.println("Invalid number of arguments: remember that you must at least specify whose board you want to view!");
            case 1,3 -> {
                int viewX = 0;
                int viewY = 0;
                if(splitArgs.length == 3){
                    try{
                        viewX = Integer.parseInt(splitArgs[1]);
                        viewY = Integer.parseInt(splitArgs[2]);
                    }catch(NumberFormatException e){
                        System.out.println("One or more of the supplied coordinates is not a number.");
                        break;
                    }
                }
                if(client.getController().getRemotePlayerNames().contains(splitArgs[0])){
                    System.out.println(CardFormatter
                            .getPlayerBoardString(client.getController().getRemotePlayerBoard(splitArgs[0]), viewX, viewY));
                }else if(client.getController().getLocalPlayerName().equals(splitArgs[0])){
                    System.out.println(CardFormatter.getPlayerBoardString(client.getController().getLocalPlayerBoard(), viewX, viewY));
                }else{
                    System.out.printf("User %s couldn't be found...\n", splitArgs[0]);
                }
            }
            default -> System.out.println("You've entered too many arguments for this command!");
        }
    }

    /**
     * Shows the hand card as specified in the written  command.
     *
     * @param argument the arguments string.
     */
    private void showHandCards(String argument){
        if(argument.equals(client.getController().getLocalPlayerName())){
            List<CardSides> hand = client.getController().getLocalPlayerHand();
            updateLocalPlayerHand(hand);
        }else if(client.getController().getRemotePlayerNames().contains(argument)){
            List<BasicCard> hand = client.getController().getRemotePlayerHand(argument);
            System.out.printf("These are %s's cards: \n", argument);
            System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(hand.stream().toList()));
        }else{
            System.out.printf("User %s couldn't be found...\n",argument);
        }
    }

    /**
     * Applies the disconnection procedures and tries to reconnect.
     */
    public void disconnectionProcedure(){
        String localPlayerName = client.getController().getLocalPlayerName();
        int gameId = client.getController().getGameId();
        disconnectionProcedure(client);
        client.getController().sendMessage(new JoinGameMessage(Status.RECONNECT, localPlayerName, null, null, gameId));
    }
}