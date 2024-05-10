package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.view.GameView;

import java.util.*;

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
                    sb.append(textColors.get(playersColors.get(recipient))).append(recipient).append(textColors.get(Content.EMPTY)).append(", ");
                }
            }
        }
        System.out.println(sb.append(": ").append(message));
    }

    @Override
    public void requestPlacement(List<CardSides> cardHand,
                          List<BasicCard> placedCards,
                          List<BasicCard> validCards,
                          List<Corner> validCorners){}

    @Override
    public void turnChanged(String turnOwner){
        System.out.println("It is now the turn of " + turnOwner);
    }

    @Override
    public void showErrorMessage(String message){}
    @Override
    public void showUserJoined(String player, Content Color){}
    @Override
    public void updateRemotePlayerHand(String player, List<BasicCard> handCards){}
    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards){}
    @Override
    public void requestStarterSide(List<CardSides> handCards){}
    @Override
    public void updateBoard(String playerName, List<BasicCard> placedCards){}
    @Override
    public void updatePersonalObjectives(List<Objective> objectives){}
    @Override
    public void updateCommonObjectives(List<Objective> objectives){}
    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards){}
    @Override
    public void notifyLastTurn(){
        System.out.println("The next turn will be the last.");
    }
    @Override
    public void revealFinalSummary(String nickname, Map<Objective,Integer> objectivePoints, int finalScore){}
    @Override
    public void revealWinners(List<String> winners){}

    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "HELP" -> System.out.println(GameParameters.getHelpBody());
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
        }
        else{
            System.out.println("Some of the recipients couldn't be found. The message wasn't sent.");
        }
    }

    private void showBoard(String arguments){

    }
}