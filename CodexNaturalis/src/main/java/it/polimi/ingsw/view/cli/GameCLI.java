package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;

import java.util.*;

public class GameCLI implements GameView {
    ClientController controller;

    public GameCLI(ClientController controller){
        this.controller = controller;

    }

    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards){
        System.out.println();
        //CardFormatter.getDeckView();
    }

    public void showChatMessage(String message, String sender, List<String> recipients){}
    public void requestPlacement(List<CardSides> cardHand,
                          List<BasicCard> placedCards,
                          List<BasicCard> validCards,
                          List<Corner> validCorners){}
    public void turnChanged(String turnOwner){}
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
     * A method that scans the terminal input of the payer.
     * If the player writes a specific command, the method satisfies it, but loops until it reads
     * a non-command input.
     * @param prompt a string representing the message prompt to print
     * @return the input read.
     */
    private String readFromInput(String prompt){
        Scanner userInput = new Scanner(System.in);
        boolean isCommand = true;
        while(isCommand){
            if(userInput.hasNext("/")){
                String inputString = userInput.nextLine();
                String[] splitString = inputString.split(" ", 2);
                switch (splitString[0].toUpperCase()){
                    case "/CHAT" -> sendChatMessage(splitString[1]);
                    case "/BOARD" -> showBoard(splitString[1]);
                }
            }
        }
        return "";
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
        List<String> recipients = Arrays.stream(splitString[0].split("/", 2)).toList();
        if(splitString[0].contains("/")){
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