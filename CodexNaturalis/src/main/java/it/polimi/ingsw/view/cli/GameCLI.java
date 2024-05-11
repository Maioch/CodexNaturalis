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
        for(Map.Entry<CardType, List<BasicCard>> entry : drawableCards.entrySet()){
            System.out.print(CardFormatter.getCardsInfoString(entry.getValue()));
        }
        System.out.println();
        List<Integer> choice = readFromInput(
                "Type what card you want to draw (as coordinates separated by spaces, starting from 0): ",
                (list -> list.size() != 2 &&
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
    public void showChatMessage(String message, String sender, List<String> recipients, Map<String, Content> playersColors){
        StringBuilder sb = new StringBuilder();
        System.out.println();
        sb.append(textColors.get(playersColors.get(sender))).append(sender).append(textColors.get(Content.EMPTY));
        if(recipients.size() != 1) {
            sb.append(" says to ");
            for (String recipient : recipients) {
                if (recipients.indexOf(recipient) != recipients.size() - 1) {
                    sb.append(textColors.get(playersColors.get(recipient)))
                            .append(recipient)
                            .append(textColors.get(Content.EMPTY))
                            .append(", ");
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
        System.out.println("These are your placed card:");
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
        System.out.println("These are your cards:");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("These are your placeable cards:");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .skip(GameParameters.getNumberOfResourceCardsInHand() + GameParameters.getNumberOfGoldCardsInHand())
                .toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(validCards.stream()
                .limit(GameParameters.getNumberOfResourceCardsInHand() + GameParameters.getNumberOfGoldCardsInHand())
                .toList()));
        System.out.println("These are the valid corner's coordinates that you can choose:");
        List<Point> validPositions = validCorners.stream().map(c -> new Point(c.getX(), c.getY())).toList();
        for(int i = 0; i < validPositions.size(); i++){
            System.out.printf("%d (%f, %f)\n", i + 1, validPositions.get(i).getX(),validPositions.get(i).getY());
        }
        System.out.println();
        int cardIndex = readFromInput("Type which card you want to place (number starting from 1): ",
                (i -> i >= 1 && i <= validCards.size()),
                this::stringToInt) - 1;
        int cornerIndex = readFromInput("Type the index of the coordinates where you want to place the card: ",
                (i -> i >= 1 && i <= validPositions.size()),
                this::stringToInt) - 1;
        controller.sendMessage(new CardPlacementMessage(validCards.get(cardIndex), validCorners.get(cornerIndex)));
    }

    @Override
    public void turnChanged(String turnOwner){
        System.out.println("It is now the turn of " + turnOwner);
    }

    @Override
    public void showErrorMessage(String message){
        System.out.println(message);
    }

    @Override
    public void showUserJoined(String player, Content color){
        System.out.println(player + " joined your game and his chosen color is " + color.toString().toLowerCase());
    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards){
        System.out.printf("These are %s's new cards\n", nickname);
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().toList()));
    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards){
        System.out.println("These are your new cards");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards){
        List<CardSides> handCards = playerCards.stream().skip(1).toList();
        CardSides starterCard = playerCards.getFirst();
        System.out.printf("These are your %d cards\n",
                GameParameters.getNumberOfResourceCardsInHand()
                + GameParameters.getNumberOfGoldCardsInHand());
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::frontSide).toList()));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(handCards.stream().map(CardSides::backSide).toList()));
        System.out.println("This is your starter card");
        System.out.print("Front side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.frontSide())));
        System.out.println("Back side:\n" + CardFormatter.getCardsInfoString(Collections.singletonList(starterCard.backSide())));
        int chosenSide = readFromInput("Choose which side you want to place (1 for front, 2 for back)",
                n -> n >= 1 && n <= 2,
                this::stringToInt);
        BasicCard chosenStarter = chosenSide == 1 ? starterCard.frontSide() : starterCard.backSide();
        controller.sendMessage(new CardPlacementMessage(chosenStarter, null));
    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards){
        System.out.printf("Here's the current placed cards by %s (centered on his last placed card):\n", nickname);
        int x = placedCards.getLast().getCorner(Location.BL).getX();
        int y = placedCards.getLast().getCorner(Location.BL).getY();
        System.out.println(CardFormatter.getPlayerBoardString(placedCards, x, y));
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

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore){
        System.out.printf("Here's a recap of %s match:\n", nickname);
        for(Map.Entry<Objective, Integer> entry : objectivePoints.entrySet()){
            System.out.print(CardFormatter.getObjectiveInfoString(entry.getKey()));
            System.out.println("Points scored: " + entry.getValue());
            System.out.println();
        }
        System.out.println("Final score: " + finalScore);
    }

    @Override
    public void revealWinners(List<String> winners){
        System.out.println("The game is over! The winner are:");
        winners.forEach(System.out::println);
    }

    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "HELP" -> System.out.println(GameParameters.getGameHelpBody());
            case "CHAT" -> sendChatMessage(argument);
            case "BOARD" -> showBoard(argument);
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
        String[] splitString = arguments.split(" ", 2);
        String delimiter = GameParameters.getDelimiter();
        List<String> recipients = Arrays.stream(splitString[0].split(delimiter, 2)).toList();
        if(!splitString[0].contains(delimiter)){
            recipients = controller.getRemotePlayerNames();
        }
        if(controller.getRemotePlayerNames().containsAll(recipients)){
            controller.sendMessage(new ChatMessage(splitString[1],null, recipients));
        }else{
            System.out.println("Some of the recipients couldn't be found. The message wasn't sent.");
        }
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
}