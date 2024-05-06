package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface GameView {
    void notifyLastTurn();
    void requestDraw(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void showChatMessage(String message, String sender, ArrayList<String> recipients);
    void requestPlacement(ArrayList<CardSides> cardHand,
                          ArrayList<BasicCard> placedCards,
                          ArrayList<BasicCard> validCards,
                          ArrayList<Corner> validCorners);
    void turnChanged(String turnOwner);
    void showErrorMessage(String message);
    void showUserJoined(String player, Content Color);
    void updateRemotePlayerHand(String player, ArrayList<BasicCard> handCards);
    void updateLocalPlayerHand(ArrayList<CardSides> handCards);
    void requestStarterSide(ArrayList<CardSides> handCards);
    void updateBoard(String playerName, ArrayList<BasicCard> placedCards);
    void updatePersonalObjectives(ArrayList<Objective> objectives);
    void updateCommonObjectives(ArrayList<Objective> objectives);
    void updateDecks(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void revealFinalSummary(String nickname, HashMap<Objective,Integer> objectivePoints, int finalScore);
    void revealWinners(List<String> winners);
}