package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;

import java.util.List;
import java.util.Map;

public class GameGUI implements GameView {

    @Override
    public void notifyLastTurn() {

    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards) {

    }

    @Override
    public void showChatMessage(ChatMessage chatMessage) {

    }

    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards, List<BasicCard> validCards, List<Corner> validCorners) {

    }

    @Override
    public void turnChanged(String turnOwner) {

    }

    @Override
    public void showErrorMessage(String message) {

    }

    @Override
    public void showUserJoined(String nickname, Content Color) {

    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {

    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {

    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards) {

    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int moveScore) {

    }

    @Override
    public void showPersonalObjectives(List<Objective> objectives) {

    }

    @Override
    public void showCommonObjectives(List<Objective> objectives) {

    }

    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards) {

    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {

    }

    @Override
    public void revealWinners(List<String> winners) {

    }

    @Override
    public void showNoMovesAvailable() {

    }

    @Override
    public void closeView() {

    }
}
