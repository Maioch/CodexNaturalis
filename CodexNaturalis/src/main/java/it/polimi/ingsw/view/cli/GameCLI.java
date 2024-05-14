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
import it.polimi.ingsw.network.messages.game.CardPlacementMessage;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.view.GameView;

import java.awt.*;
import java.util.*;
import java.util.List;

public class GameCLI extends AbstractCLI implements GameView {
    private final ClientController controller;

    public GameCLI(ClientController controller){
        this.controller = controller;
    }

    /**
     * Method that asks for a draw choice
     * @param drawableCards the list of drawable options
     */
    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards){
        System.out.println();
        System.out.println("These are the cards you can draw:");
        int i = 0;
        for(CardType cardType : CardType.values()){
            List<BasicCard> deckCardList = drawableCards.get(cardType);
            if(deckCardList != null && !deckCardList.isEmpty()) {
                System.out.println(i);
                BasicCard cardOnTop = deckCardList.getFirst();
                List<BasicCard> cardOnTopList = new ArrayList<>();
                cardOnTopList.add(cardOnTop);
                System.out.print(CardFormatter.getCardsInfoString(cardOnTopList, true));
                System.out.print(CardFormatter.getCardsInfoString(deckCardList.subList(1,deckCardList.size()),false));
            }
            i++;
        }
        System.out.println();
        List<Integer> choice = readFromInput(
                "Type what card you want to draw (as coordinates separated by spaces, starting from 0): ",
                (list -> list.size() == 2 &&
                        list.getFirst() >= 0 && list.getFirst() <= 1 &&
                        list.getLast() >= 0 && list.getLast() <= GameParameters.getNumberOfVisibleCards()),
                this::stringToListInt);
        controller.sendMessage(new DrawChoiceMessage(choice.getLast(), CardType.values()[choice.getFirst()]));
    }

    /**
     * A method that shows (prints) a message
     * @param message the content of the message
     * @param sender the sender of the message
     * @param recipients the recipients of the message
     */
    @Override
    public void showChatMessage(String message, String sender, List<String> recipients){
        if(!sender.equals(controller.getLocalPlayerName())){
            printChatMessage(message,sender,recipients);
        }
    }

    private void printChatMessage(String message, String sender, List<String> recipients){
        StringBuilder sb = new StringBuilder();
        Map<String, Content> playerColors = controller.getPlayerColors();
        sb.append(textColors.get(playerColors.get(sender))).append(sender).append(textColors.get(Content.EMPTY));
        if(recipients.size() != controller.getRemotePlayerNames().size()) {
            sb.append(" says to ");
            for (String recipient : recipients) {
                sb.append(textColors.get(playerColors.get(recipient)))
                        .append(recipient)
                        .append(textColors.get(Content.EMPTY));
                if(!recipient.equals(recipients.getLast())){
                    sb.append(", ");
                }
            }
        }
        System.out.println(sb.append(": ").append(message));
    }

    @Override
    public void requestPlacement(List<CardSides> handCards,
                                 List<BasicCard> placedCards,
                                 List<BasicCard> validCards,
                                 List<Corner> validCorners){
        System.out.println("These are your cards:");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList(), false));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList(),true));
        System.out.println("These are your placeable cards:");
        int numberOfBackSides = GameParameters.getNumberOfResourceCardsInHand() + GameParameters.getNumberOfGoldCardsInHand();
        int maxCardsInHand = (numberOfBackSides) * 2;
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .limit( numberOfBackSides - maxCardsInHand + validCards.size())
                .toList(), false));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .skip(numberOfBackSides - maxCardsInHand + validCards.size())
                .toList(), true));
        int cardIndex = readFromInput("Type which card you want to place (number starting from 1): ",
                (i -> i >= 1 && i <= validCards.size()),
                this::stringToInt) - 1;
        System.out.println("These are your placed card:");
        System.out.println(placedCards);
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.println("These are the valid corner's coordinates that you can choose:");
        List<Point> validPositions = validCorners.stream().map(c -> new Point(c.getX(), c.getY())).toList();
        for(int i = 0; i < validPositions.size(); i++){
            System.out.printf("%d (%f, %f)\n", i + 1, validPositions.get(i).getX(),validPositions.get(i).getY());
        }
        System.out.println();
        int cornerIndex = readFromInput("Type the index of the coordinates where you want to place the card: ",
                (i -> i >= 1 && i <= validPositions.size()),
                this::stringToInt) - 1;
        controller.sendMessage(new CardPlacementMessage(validCards.get(cardIndex), validCorners.get(cornerIndex)));
    }

    @Override
    public void turnChanged(String turnOwner){
        System.out.printf("%s is playing their turn...\n",turnOwner);
    }

    @Override
    public void showErrorMessage(String message){
        System.out.println(message);
    }

    @Override
    public void showUserJoined(String player, Content color){
        if (!player.equals(controller.getLocalPlayerName())) {
            System.out.println(player + " joined your game and their chosen color is " + color.toString().toLowerCase());
        }
    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards){
        System.out.printf("These are %s's new cards\n", nickname);
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().toList(), true));
    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards){
        System.out.println("These are your new cards");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList(), false));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList(), true));
    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards){
        List<CardSides> handCards = playerCards.stream().skip(1).toList();
        CardSides starterCard = playerCards.getFirst();
        System.out.printf("These are your %d cards\n",
                GameParameters.getNumberOfResourceCardsInHand()
                        + GameParameters.getNumberOfGoldCardsInHand());
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList(), false));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList(), true));
        System.out.println("This is your starter card");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.frontSide()), false));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.backSide()), true));
        int chosenSide = readFromInput("Choose which side you want to place (1 for front, 2 for back)",
                n -> n >= 1 && n <= 2,
                this::stringToInt);
        BasicCard chosenStarter = chosenSide == 1 ? starterCard.frontSide() : starterCard.backSide();
        controller.sendMessage(new CardPlacementMessage(chosenStarter, null));
    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int moveScore){
        System.out.printf("Here's the current placed cards by %s (centered on his last placed card):\n", nickname);
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.printf("Their new score is: %d\n", moveScore);
    }

    @Override
    public void showPersonalObjectives(List<Objective> objectives){
        System.out.println("Personal objectives:");
        for(Objective objective : objectives){
            System.out.print(CardFormatter.getObjectiveInfoString(objective));
        }
    }

    @Override
    public void showCommonObjectives(List<Objective> objectives){
        System.out.println("Common objectives:");
        for(Objective objective : objectives){
            System.out.print(CardFormatter.getObjectiveInfoString(objective));
        }
    }

    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards){
        //not implemented for the TUI to prevent message flooding
    }

    @Override
    public void notifyLastTurn(){
        System.out.println("The next turn will be the last.");
    }

    private void showSymbols(){
        for(Content content : Content.values()){
            if(content != Content.EMPTY){
                System.out.printf("%s : %s\n", content.getSymbol(), content.name());
            }
        }
    }

    private void showObjectives(){
        showCommonObjectives(controller.getCommonObjectives());
        showPersonalObjectives(controller.getPersonalObjectives());
    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore){
        System.out.printf("Here's a recap of %s's match:\n", nickname);
        for(Map.Entry<Objective, Integer> entry : objectivePoints.entrySet()){
            System.out.print(CardFormatter.getObjectiveInfoString(entry.getKey()));
            System.out.println("Points scored: " + entry.getValue());
            System.out.println();
        }
        System.out.println("Final score: " + finalScore);
    }

    @Override
    public void revealWinners(List<String> winners){
        System.out.println("The game is over! The winners are:");
        winners.forEach(System.out::println);
        String back = readFromInput("Type BACK when you're ready to return to the main menu: ",
                s -> s.equalsIgnoreCase("back"),
                this::stringIdentity);
        controller.backToSetup();
    }

    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "HELP" -> System.out.println(GameParameters.getGameHelpBody());
            case "CHAT" -> sendChatMessage(argument);
            case "BOARD" -> showBoard(argument);
            case "SYMBOLS" -> showSymbols();
            case "OBJECTIVES" -> showObjectives();
            default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
        }
    }

    /**
     * Method that computes the chat input, in order to send it.
     * The chat terminal convention is the following: "/CHAT nick1/nick2/... messageInput"
     * The "nicks", are the nicknames of the players to which the message will be sent.
     * If the nicknames aren't specified, the message will be sent to all the players.
     * The nicks MUST be interspersed by "/" slashes.
     *
     * @param arguments the entire string input that comes after "/CHAT" label-command
     */
    @SuppressWarnings("SlowListContainsAll")
    private void sendChatMessage(String arguments){
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
        switch (splitArgs.length){
            case 0,2 -> System.out.println("Invalid number of arguments. remember that you must at least specify whose board you want to view");
            case 1,3 -> {
                int viewX = 0;
                int viewY = 0;
                if(splitArgs.length == 3){
                    try {
                        viewX = Integer.parseInt(splitArgs[1]);
                        viewY = Integer.parseInt(splitArgs[2]);
                    } catch (NumberFormatException e){
                        System.out.println("One or more of the supplied coordinates is not a number");
                        break;
                    }
                }
                if(controller.getRemotePlayerNames().contains(splitArgs[0])){
                    System.out.println(CardFormatter.getPlayerBoardString(controller.getRemotePlayerBoard(splitArgs[0]),viewX,viewY));
                } else if (controller.getLocalPlayerName().equals(splitArgs[0])) {
                    System.out.println(CardFormatter.getPlayerBoardString(controller.getLocalPlayerBoard(),viewX,viewY));
                } else{
                    System.out.printf("%s is not a player in the current game.\n",splitArgs[0]);
                }
            }
            default -> System.out.println("You've entered too many arguments for this command!");
        }
    }

    @Override
    public void closeView() {

    }

}