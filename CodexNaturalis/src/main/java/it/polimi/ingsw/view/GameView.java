package it.polimi.ingsw.view;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.messages.game.ChatMessage;

import java.util.List;
import java.util.Map;

public interface GameView {
    void notifyLastTurn();
    void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft);
    void showChatMessage(ChatMessage chatMessage);
    void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards);
    void turnChanged(String turnOwner);
    void showErrorMessage(String message);
    void showUserJoined(String nickname, Content color, boolean isGameFull);
    void updateRemotePlayerHand(String nickname, List<BasicCard> handCards);
    void updateLocalPlayerHand(List<CardSides> handCards);
    void requestStarterSide(List<CardSides> playerCards);
    void updateBoard(String nickname, List<BasicCard> placedCards, int score);
    void requestPersonalObjectivesChoice(List<Objective> objectives);
    void showPersonalObjectives(List<Objective> objectives);
    void showCommonObjectives(List<Objective> objectives);
    void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType,Integer> numberOfCardsLeft);
    void revealFinalSummary(String nickname, Map<Objective,Integer> objectivePoints, int finalScore);
    void revealWinners(List<String> winners);
    void notifyRemotePlayerDisconnected(String nickname, Content color);
    void notifyPlayerLeftLobby(String nickname, Content color);
    void notifyRemotePlayerReconnected(String nickname);
    void notifyGameTimeout();
    void notifyGameCanceled();
    void notifyTurnSkipped();
    void showNoMovesAvailable();
    void showDisconnectionMessage();
    void closeView();
}