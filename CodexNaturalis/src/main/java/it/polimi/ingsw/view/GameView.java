package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;

import java.util.ArrayList;
import java.util.HashMap;

public interface GameView {
    void turnChanged(String turnOwner);
    void showErrorMessage(String message);
    void requestStarterSide(ArrayList<CardSides> handCards);
    void updateDrawableCards(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void updateBoard(String playerName, ArrayList<BasicCard> placedCards);
    void updateDecks(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void declareWinner(String playerName);
}