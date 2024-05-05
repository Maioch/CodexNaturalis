package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface GameView {
    void showGameEndScreen(List<String> winningPlayers);
    void notifyLastTurn();
    void requestDraw();
    void showChatMessage(String message, String sender, ArrayList<String> recipients);
    void requestPlacement(ArrayList<BasicCard> validCards, ArrayList<Corner> validCorners);
    void turnChanged(String turnOwner);
    void showErrorMessage(String message);
    void requestStarterSide(ArrayList<CardSides> handCards);
    void updateDrawableCards(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void updateBoard(String playerName, ArrayList<BasicCard> placedCards);
    void updateDecks(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
}