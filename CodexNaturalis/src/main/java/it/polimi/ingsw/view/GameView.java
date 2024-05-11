package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;

import java.util.List;
import java.util.Map;

public interface GameView {
    void notifyLastTurn();
    void requestDraw(Map<CardType, List<BasicCard>> drawableCards);
    void showChatMessage(String message, String sender, List<String> recipients, Map<String, Content> playersColors);
    void requestPlacement(List<CardSides> handCards,
                          List<BasicCard> placedCards,
                          List<BasicCard> validCards,
                          List<Corner> validCorners);
    void turnChanged(String turnOwner);
    void showErrorMessage(String message);
    void showUserJoined(String nickname, Content Color);
    void updateRemotePlayerHand(String nickname, List<BasicCard> handCards);
    void updateLocalPlayerHand(List<CardSides> handCards);
    void requestStarterSide(List<CardSides> playerCards);
    void updateBoard(String nickname, List<BasicCard> placedCards);
    void showPersonalObjectives(List<Objective> objectives);
    void showCommonObjectives(List<Objective> objectives);
    void updateDecks(Map<CardType, List<BasicCard>> drawableCards);
    void revealFinalSummary(String nickname, Map<Objective,Integer> objectivePoints, int finalScore);
    void revealWinners(List<String> winners);
}