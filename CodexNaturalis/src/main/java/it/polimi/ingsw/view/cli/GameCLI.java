package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.view.GameView;

import java.util.*;

public class GameCLI implements GameView {
    private final ClientController controller;

    private static final Map<Content, String> textColors = new HashMap<>(){{
        put(Content.RED, "\\u001B[31m");
        put(Content.BLUE, "\\u001B[34m");
        put(Content.GREEN, "\\u001B[32m");
        put(Content.PURPLE, "\\u001B[35m");
        put(Content.EMPTY, "\\u001B[0m");
    }};


    public GameCLI(ClientController controller){
        this.controller = controller;
    }

    /**
     * Method that asks for a draw choice
     * @param drawableCards the list of drawable options
     */
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards){
        System.out.println();
        for(Map.Entry<CardType, List<BasicCard>> entry : drawableCards.entrySet()){
            System.out.print(CardFormatter.getCardsInfoString(entry.getValue()));
        }
        System.out.println();
        System.out.print("Type what card you want to draw (as coordinates separated by spaces, starting from 0): ");
        String choice = UtilitiesCLI.getUserStringChoice(3, "choice");
        if(choice.split(" ", 2).length != 2){
            controller.sendMessage(new DrawChoiceMessage(-1, null));
            return;
        }
        int cardType;
        int index;
        try{
            cardType = Integer.parseInt(choice.split(" ", 2)[0]);
            index = Integer.parseInt(choice.split(" ", 2)[1]);
        }catch(NumberFormatException e){
            controller.sendMessage(new DrawChoiceMessage(-1, null));
            return;
        }
        if(cardType != 0 && cardType != 1){
            controller.sendMessage(new DrawChoiceMessage(-1, null));
            return;
        }
        controller.sendMessage(new DrawChoiceMessage(index, cardType == 0 ? CardType.RESOURCE : CardType.GOLD));
    }

    /**
     * A method that shows (prints) a message
     * @param message the content of the message
     * @param sender the sender of the message
     * @param recipients the recipients of the message
     */
    public void showChatMessage(String message, String sender, List<String> recipients, Map<String, Content> playersColors){
        StringBuilder sb = new StringBuilder();
        System.out.println();
        sb.append(textColors.get(playersColors.get(sender))).append(sender).append(textColors.get(Content.EMPTY));
        if(recipients.size() != 1) {
            sb.append(" says to ");
            for (String recipient : recipients) {
                if (recipients.indexOf(recipient) != recipients.size() - 1) {
                    sb.append(textColors.get(playersColors.get(recipient))).append(recipient).append(textColors.get(Content.EMPTY)).append(", ");
                }
            }
        }
        System.out.println(sb.append(": ").append(message));
    }

    public void requestPlacement(List<CardSides> cardHand,
                          List<BasicCard> placedCards,
                          List<BasicCard> validCards,
                          List<Corner> validCorners){}

    public void turnChanged(String turnOwner){
        System.out.println("It is now the turn of " + turnOwner);
    }

    public void showErrorMessage(String message){}
    public void showUserJoined(String player, Content Color){}
    public void updateRemotePlayerHand(String player, List<BasicCard> handCards){}
    public void updateLocalPlayerHand(List<CardSides> handCards){}
    public void requestStarterSide(List<CardSides> handCards){}
    public void updateBoard(String playerName, List<BasicCard> placedCards){}
    public void updatePersonalObjectives(List<Objective> objectives){}
    public void updateCommonObjectives(List<Objective> objectives){}
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards){}

    public void notifyLastTurn(){
        System.out.println("The next turn will be the last.");
    }

    public void revealFinalSummary(String nickname, Map<Objective,Integer> objectivePoints, int finalScore){}
    public void revealWinners(List<String> winners){}

    /**
     * A method that scans the terminal input of the player.
     * If the player writes a specific command, the method satisfies it, but loops until it reads
     * a non-command input.
     * @param prompt a string representing the message prompt to print
     * @return the input read.
     */
    private String readFromInput(String prompt){
        String commandChar = GameParameters.getCommandChar();
        while(true){
            System.out.println(prompt);
            String inputString = UtilitiesCLI.getUserStringChoice();
            if(inputString.indexOf(commandChar) != 0){
                return inputString;
            }
            String[] splitString = inputString.split(" ", 2);
            String command = splitString[0];
            String argument = (splitString.length > 1) ? splitString[1] : "";
            switch (command.toUpperCase().substring(commandChar.length())){
                case "/HELP" -> System.out.println(GameParameters.getHelpBody());
                case "/CHAT" -> sendChatMessage(argument);
                case "/BOARD" -> showBoard(argument);
                default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
            }
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
        }
        else{
            System.out.println("Some of the recipients couldn't be found. The message wasn't sent.");
        }
    }

    private void showBoard(String arguments){

    }
}