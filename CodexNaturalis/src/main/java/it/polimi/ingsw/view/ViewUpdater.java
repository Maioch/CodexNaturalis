package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;

import java.util.ArrayList;
import java.util.HashMap;

public interface ViewUpdater {
    void updateBoard(String nickname, ArrayList<BasicCard> placedCards);
    void updateDecks(HashMap<CardType, ArrayList<BasicCard>> drawableCards);
    void declareWinner(String nickname);
}