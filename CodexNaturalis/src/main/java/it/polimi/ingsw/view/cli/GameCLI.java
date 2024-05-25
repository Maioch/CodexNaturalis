package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.CardPlacementMessage;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.network.messages.game.ObjectivesMessage;
import it.polimi.ingsw.view.GameView;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * The CLI associated to the gameplay phase.
 */
public class GameCLI extends AbstractCLI implements GameView {
    private final ClientController controller;
    private Thread readInputThread;
    private final Queue<ChatMessage> chatMessageQueue;

    /**
     * Constructor for the class.
     * @param controller the controller for the client that will be using the CLI.
     */
    public GameCLI(ClientController controller) {
        this.controller = controller;
        chatMessageQueue = new LinkedList<>();
    }

    /**
     * Method that asks for a draw choice.
     * @param drawableCards the list of drawable options.
     */
    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards){
        System.out.println();
        System.out.println("Here are the cards you can draw: ");
        int i = 1;
        for(CardType cardType : CardType.values()){
            List<BasicCard> deckCardList = drawableCards.get(cardType);
            if(deckCardList != null && !deckCardList.isEmpty()) {
                System.out.println(i);
                String cardOnTop = CardFormatter.getCardString(deckCardList.getFirst());
                System.out.print(cardOnTop.isEmpty() ? "\n\nThe deck is empty\n\n" : cardOnTop);
                System.out.print(CardFormatter.getCardsInfoString(deckCardList.subList(1,deckCardList.size())));
            }
            i++;
        }
        System.out.println();
        List<Integer> choice = readFromInput(
                "Choose which card you want to draw by writing its coordinates separated by a space (starting from 1): ",
                (list -> list.size() == 2 &&
                        list.getFirst() >= 1 && list.getFirst() <= 2 &&
                        list.getLast() >= 1 && list.getLast() <= GameParameters.getNumberOfVisibleCards() + 1),
                this::stringToListInt);
        controller.sendMessage(new DrawChoiceMessage(choice.getLast() - 1, CardType.values()[choice.getFirst() - 1]));
    }

    /**
     * A method that shows (prints) a message.
     * @param chatMessage contains the sender, the recipients and the message.
     */
    @Override
    public void showChatMessage(ChatMessage chatMessage){
        if(!chatMessage.getSender().equals(controller.getLocalPlayerName())){
            if(controller.getPlayerWithTurn().equals(controller.getLocalPlayerName())){
                chatMessageQueue.add(chatMessage);
            }else{
                printChatMessage(chatMessage.getMessage(), chatMessage.getSender(), chatMessage.getRecipients());
            }
        }
    }

    /**
     * Method that builds the default message string, used by the method above to print a message.
     * @param message the client's message
     * @param sender the sender of the message.
     * @param recipients the recipients of the message.
     */
    private void printChatMessage(String message, String sender, List<String> recipients){
        StringBuilder sb = new StringBuilder();
        Map<String, Content> playerColors = controller.getPlayerColors();
        sb.append(playerColors.get(sender).getTextColorString())
                .append(sender)
                .append(Content.EMPTY.getTextColorString());
        if(recipients.size() != controller.getRemotePlayerNames().size()) {
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
     * Method that requests the client to place a card.
     * @param handCards the client's hand cards.
     * @param placedCards the client's placed cards.
     * @param validCards the cards that can be placed.
     * @param validCorners the corners where the new card can be placed.
     */
    @Override
    public void requestPlacement(List<CardSides> handCards,
                                 List<BasicCard> placedCards,
                                 List<BasicCard> validCards,
                                 List<Corner> validCorners){
        System.out.println("These are the cards in your hand: ");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("Here are the ones you can place: ");
        int numberOfBackSides = GameParameters.getNumberOfResourceCardsInHand() + GameParameters.getNumberOfGoldCardsInHand();
        int maxCardsInHand = (numberOfBackSides) * 2;
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .limit( numberOfBackSides - maxCardsInHand + validCards.size())
                .toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .skip(numberOfBackSides - maxCardsInHand + validCards.size())
                .toList()));
        int cardIndex = readFromInput("Choose which card you want to place by writing its index (starting from 1): ",
                (i -> i >= 1 && i <= validCards.size()),
                this::stringToInt) - 1;
        System.out.println("These are the cards placed on your board: ");
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.println("Here are the coordinates of all the corners where you can place the card: ");
        List<Point> validPositions = validCorners.stream().map(c -> new Point(c.getX(), c.getY())).toList();
        for(int i = 0; i < validPositions.size(); i++){
            System.out.printf("%d (%.0f, %.0f)\n", i + 1, validPositions.get(i).getX(),validPositions.get(i).getY());
        }
        System.out.println();
        int cornerIndex = readFromInput("Choose the coordinates you prefer by typing their index: ",
                (i -> i >= 1 && i <= validPositions.size()),
                this::stringToInt) - 1;
        controller.sendMessage(new CardPlacementMessage(validCards.get(cardIndex), validCorners.get(cornerIndex)));
    }

    /**
     * Method that notifies the client about a turn change.
     * @param turnOwner the client that is playing its turn.
     */
    @Override
    public void turnChanged(String turnOwner){
        while(!chatMessageQueue.isEmpty()){
            ChatMessage chatMessage = chatMessageQueue.poll();
            printChatMessage(chatMessage.getMessage(),chatMessage.getSender(),chatMessage.getRecipients());
        }
        Map<String, Content> playerColors = controller.getPlayerColors();
        String coloredTurnOwner =  playerColors.get(turnOwner).getTextColorString() + turnOwner + Content.EMPTY.getTextColorString();
        if(!turnOwner.equals(controller.getLocalPlayerName())) {
            System.out.printf("%s is playing their turn...\n", coloredTurnOwner);
            if(readInputThread == null || !readInputThread.isAlive()) {
                readInputThread = new Thread(() -> readFromInput("", ((c) -> false), this::stringIdentity));
                readInputThread.start();
            }
            return;
        }
        System.out.printf("It's your turn, %s!\n", coloredTurnOwner);
        if (readInputThread != null) {
            readInputThread.interrupt();
        }
    }

    /**
     * Method that prints an error message.
     * @param message the message to print.
     */
    @Override
    public void showErrorMessage(String message){
        System.out.println(message);
    }

    /**
     * Method that notifies the client about a new player that joined its game.
     * @param player the new player's nickname.
     * @param color the new player's color.
     */
    @Override
    public void showUserJoined(String player, Content color){
        if (!player.equals(controller.getLocalPlayerName())) {
            System.out.println(color.getTextColorString() + player + Content.EMPTY.getTextColorString() + " joined your game!");
        }
    }

    /**
     * Method not implemented for the TUI to prevent message flooding.
     * @param nickname //
     * @param handCards //
     */
    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards){
    }

    /**
     * Method used to update the client about its current hand.
     * @param handCards the player's hand cards.
     */
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards){
        System.out.println("These are the cards in your hand: ");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(
                handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(
                handCards.stream().map(CardSides::backSide).toList()));
    }

    /**
     * Method used to request the starter card's placement at the start of the match.
     * @param playerCards the list of cards owned by the player.
     */
    @Override
    public void requestStarterSide(List<CardSides> playerCards){
        List<CardSides> handCards = playerCards.stream().skip(1).toList();
        CardSides starterCard = playerCards.getFirst();
        System.out.printf("These are the %d cards in your hand: \n",
                GameParameters.getNumberOfResourceCardsInHand()
                        + GameParameters.getNumberOfGoldCardsInHand());
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("This is your starter card: ");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.frontSide())));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.backSide())));
        int chosenSide = readFromInput("Choose which side of your starter you want to place (1 for front, 2 for back): ",
                n -> n >= 1 && n <= 2,
                this::stringToInt);
        BasicCard chosenStarter = chosenSide == 1 ? starterCard.frontSide() : starterCard.backSide();
        controller.sendMessage(new CardPlacementMessage(chosenStarter, null));
    }

    /**
     * Method used to update the client about another player's board and score.
     * @param nickname the player's nickname.
     * @param placedCards the players board.
     * @param moveScore the player's score.
     */
    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int moveScore){
        System.out.printf("Here are %s's current placed cards (centered on his last placed card): \n",
                controller.getPlayerColors().get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.printf("Their new score is: %d\n", moveScore);
    }

    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        System.out.println("These are the secret objectives you can choose from:");
        for(int i = 0; i < objectives.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, CardFormatter.getObjectiveInfoString(objectives.get(i)));
        }
        int numberOfSecretObjectives = GameParameters.getNumberOfSecretObjectives();
        List<Integer> chosenObjective = readFromInput(
                String.format("Choose %d of these: ", numberOfSecretObjectives),
                l -> l.stream().allMatch(i -> i >= 1 && i <= objectives.size()) && l.size() == numberOfSecretObjectives,
                this::stringToListInt);
        controller.sendMessage(new ObjectivesMessage(Status.SECRET_OBJECTIVES,
                chosenObjective.stream().map(i -> objectives.get(i - 1)).toList(),
                new ArrayList<>()));
    }

    /**
     * Method used to show the client its personal objectives.
     * @param objectives the player's personal objectives.
     */
    @Override
    public void showPersonalObjectives(List<Objective> objectives){
        System.out.println("These are your personal objectives: ");
        for(Objective objective : objectives){
            System.out.print(CardFormatter.getObjectiveInfoString(objective));
        }
    }

    /**
     * Method used to show the client the common objectives.
     * @param objectives the common objectives.
     */
    @Override
    public void showCommonObjectives(List<Objective> objectives){
        System.out.println("These are the objectives shared by you and the other players: ");
        for(Objective objective : objectives){
            System.out.print(CardFormatter.getObjectiveInfoString(objective));
        }
    }

    /**
     * Method not implemented for the TUI to prevent message flooding.
     * @param drawableCards //
     */
    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards){}

    /**
     * Method used to notify the players that it's time for the last game's turn.
     */
    @Override
    public void notifyLastTurn(){
        System.out.println("The next turn will be the last.");
    }

    /**
     * Method used to notify the players that the turn has been skipped.
     */
    @Override
    public void notifyTurnSkipped() {
        System.out.println("The turn has been skipped because the player isn't connected");
    }

    @Override
    public void notifyGameTimeout(){
        System.out.printf(
                "You're the only player left. If no players reconnect in the next %d seconds, you'll win by forfeit.\n",
                GameParameters.getForfeitTime());
    }

    /**
     * Method used to show the client a legend of the symbols printed in the CLI.
     */
    private void showSymbols(){
        for(Content content : Content.values()){
            if(content != Content.EMPTY){
                System.out.printf("%s : %s\n", content.getSymbol(), content.name());
            }
        }
    }

    /**
     * Method used to show the client all the objectives, both common and personal.
     */
    private void showObjectives(){
        showCommonObjectives(controller.getCommonObjectives());
        showPersonalObjectives(controller.getPersonalObjectives());
    }

    /**
     * Method used to show the client a player's game recap.
     * @param nickname the player's nickname.
     * @param objectivePoints a map containing all the player's objectives and related scores.
     * @param finalScore the player's final score.
     */
    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore){
        System.out.printf("Here's a recap of %s's match: \n",
                controller.getPlayerColors().get(nickname).getTextColorString() + nickname + Content.EMPTY.getTextColorString());
        for(Map.Entry<Objective, Integer> entry : objectivePoints.entrySet()){
            System.out.print(CardFormatter.getObjectiveInfoString(entry.getKey()));
            System.out.println("Points gained through this objective: " + entry.getValue());
            System.out.println();
        }
        System.out.println("Final score: " + finalScore);
    }

    /**
     * Method used to show game's winners to the client; it also enables the client to return to the main menu.
     * @param winners the list of winners.
     */
    @Override
    public void revealWinners(List<String> winners){
        if (readInputThread != null) {
            readInputThread.interrupt();
        }
        System.out.println("The game is over! The winner is:");
        winners.forEach(System.out::println);
        readFromInput("Type BACK when you're ready to return to the main menu: ",
                s -> s.equalsIgnoreCase("back"),
                this::stringIdentity);
        controller.backToSetup();
    }

    @Override
    public void notifyGameCanceled(){
        System.out.println("The game took too long to start, and it has been canceled.");
        controller.backToSetup();
    }

    /**
     * Method used to handle all the possible client's commands.
     * @param command the command to handle.
     * @param argument the arguments associated to the command.
     */
    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "HELP" -> System.out.println(GameParameters.getGameHelpBody());
            case "CHAT" -> sendChatMessage(argument);
            case "BOARD" -> showBoard(argument);
            case "HAND" -> showHandCards(argument);
            case "SYMBOLS" -> showSymbols();
            case "OBJECTIVES" -> showObjectives();
            default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
        }
    }

    /**
     * Method used to notify the client that someone is out of moves.
     */
    @Override
    public void showNoMovesAvailable() {
        Map<String, Content> playerColors = controller.getPlayerColors();
        String localPlayerName = controller.getLocalPlayerName();
        String turnOwner = controller.getPlayerWithTurn();
        if(turnOwner.equals(localPlayerName)){
            System.out.printf("%s, you can no longer make any more moves ;(\n",
                    playerColors.get(localPlayerName).getTextColorString() + turnOwner + Content.EMPTY.getTextColorString());
        }else{
            System.out.printf("%s cannot make any more moves ;)\n",
                    playerColors.get(turnOwner).getTextColorString() + turnOwner + Content.EMPTY.getTextColorString());
        }
    }

    @Override
    public void closeView() {

    }

    /**
     * Method that computes the chat input, in order to send it.
     * The chat terminal convention is the following: "/CHAT nick1/nick2/... messageInput"
     * The "nicks", are the nicknames of the players to which the message will be sent.
     * If the nicknames aren't specified, the message will be sent to all the players.
     * The nicks MUST be interspersed by "/" slashes.
     * @param arguments the entire string input that comes after "/CHAT" label-command.
     */
    @SuppressWarnings("SlowListContainsAll")
    private void sendChatMessage(String arguments){
        if(controller.getPlayerWithTurn().equals(controller.getLocalPlayerName())){
            System.out.println("You cannot send chat messages while your turn is in progress >:/");
            return;
        }
        List<String> recipients = extractRecipients(arguments);
        String[] splitArgs = arguments.split(" ",2);
        String chatMessage = !recipients.isEmpty() ? splitArgs[1] : arguments;
        if(recipients.isEmpty()){
            recipients = controller.getRemotePlayerNames();
        }
        if(controller.getRemotePlayerNames().containsAll(recipients)){
            controller.sendMessage(new ChatMessage(chatMessage,null, recipients));
            printChatMessage(chatMessage, controller.getLocalPlayerName(), recipients);
        }else{
            System.out.println("Some of the recipients couldn't be found. The message wasn't sent.");
        }
    }

    private List<String> extractRecipients(String arguments){
        List<String> recipients = new ArrayList<>();
        int indexOfDelimiter = 0;
        while(indexOfDelimiter != -1){
            indexOfDelimiter = arguments.indexOf(GameParameters.getDelimiter(),indexOfDelimiter);
            if(indexOfDelimiter != -1){
                int indexOfNextDelimiter = arguments.indexOf(GameParameters.getDelimiter(),indexOfDelimiter + 1);
                if(indexOfNextDelimiter != -1){
                    recipients.add(arguments.substring(indexOfDelimiter + 1,indexOfNextDelimiter));
                }
                indexOfDelimiter = indexOfNextDelimiter;
            }
        }
        return recipients;
    }

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
                        System.out.println("One or more of the supplied coordinates is not a number");
                        break;
                    }
                }
                if(controller.getRemotePlayerNames().contains(splitArgs[0])){
                    System.out.println(CardFormatter.getPlayerBoardString(controller.getRemotePlayerBoard(splitArgs[0]), viewX, viewY));
                }else if(controller.getLocalPlayerName().equals(splitArgs[0])){
                    System.out.println(CardFormatter.getPlayerBoardString(controller.getLocalPlayerBoard(), viewX, viewY));
                }else{
                    System.out.printf("User %s couldn't be found...\n", splitArgs[0]);
                }
            }
            default -> System.out.println("You've entered too many arguments for this command!");
        }
    }

    private void showHandCards(String argument){
        if(argument.equals(controller.getLocalPlayerName())){
            List<CardSides> hand = controller.getLocalPlayerHand();
            updateLocalPlayerHand(hand);
        }else if(controller.getRemotePlayerNames().contains(argument)){
            List<BasicCard> hand = controller.getRemotePlayerHand(argument);
            System.out.printf("These are %s's cards: \n", argument);
            System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(hand.stream().toList()));
        }else{
            System.out.printf("User %s couldn't be found...\n",argument);
        }
    }
}