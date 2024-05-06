package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.view.GameView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GameCLI implements GameView {
    public GameCLI(){

    }

    public void requestDraw(HashMap<CardType, ArrayList<BasicCard>> drawableCards){
        System.out.println();
        //CardFormatter.getDeckView();
    }

    public void showChatMessage(String message, String sender, ArrayList<String> recipients){}
    public void requestPlacement(ArrayList<CardSides> cardHand,
                          ArrayList<BasicCard> placedCards,
                          ArrayList<BasicCard> validCards,
                          ArrayList<Corner> validCorners){}
    public void turnChanged(String turnOwner){}
    public void showErrorMessage(String message){}
    public void showUserJoined(String player, Content Color){}
    public void updateRemotePlayerHand(String player, ArrayList<BasicCard> handCards){}
    public void updateLocalPlayerHand(ArrayList<CardSides> handCards){}
    public void requestStarterSide(ArrayList<CardSides> handCards){}
    public void updateBoard(String playerName, ArrayList<BasicCard> placedCards){}
    public void updatePersonalObjectives(ArrayList<Objective> objectives){}
    public void updateCommonObjectives(ArrayList<Objective> objectives){}
    public void updateDecks(HashMap<CardType, ArrayList<BasicCard>> drawableCards){}

    public void notifyLastTurn(){
        System.out.println("The next turn will be the last.");
    }

    public void revealFinalSummary(String nickname, HashMap<Objective,Integer> objectivePoints, int finalScore){}
    public void revealWinners(List<String> winners){}

    private String readFromInput(){
        return "temp";
    }
}
