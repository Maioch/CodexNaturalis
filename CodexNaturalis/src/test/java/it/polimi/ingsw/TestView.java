package it.polimi.ingsw;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.shared.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestView implements SetupView, GameView {

    private List<Pair<String, List<Object>>> recentCalls = new ArrayList<>();

    public TestView() {}

    public List<Pair<String, List<Object>>> getRecentCalls() {
        List<Pair<String, List<Object>>> methods = new ArrayList<>(recentCalls);
        recentCalls = new ArrayList<>();
        return methods;
    }

    @Override
    public void notifyLastTurn() {
        recentCalls.add(new Pair<>("notifyLastTurn", new ArrayList<>()));
    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        recentCalls.add(new Pair<>("requestDraw", List.of(drawableCards, numberOfCardsLeft)));
    }

    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        recentCalls.add(new Pair<>("showChatMessage", List.of(chatMessage)));
    }

    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards) {
        recentCalls.add(new Pair<>("requestPlacement", List.of(handCards, placedCards)));
    }

    @Override
    public void turnChanged(String turnOwner) {
        recentCalls.add(new Pair<>("turnChanged", List.of(turnOwner)));
    }

    @Override
    public void showErrorMessage(String message) {
        recentCalls.add(new Pair<>("showErrorMessage", List.of(message)));
    }

    @Override
    public void showUserJoined(String nickname, Content color, boolean isGameFull) {
        recentCalls.add(new Pair<>("showUserJoined", List.of(nickname, color, isGameFull)));
    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {
        recentCalls.add(new Pair<>("updateRemotePlayerHand", List.of(nickname, handCards)));
    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {
        recentCalls.add(new Pair<>("updateLocalPlayerHand", List.of(handCards)));
    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards) {
        recentCalls.add(new Pair<>("requestStarterSide", List.of(playerCards)));
    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int score) {
        recentCalls.add(new Pair<>("updateBoard", List.of(nickname, placedCards, score)));
    }

    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        recentCalls.add(new Pair<>("requestPersonalObjectivesChoice", List.of(objectives)));
    }

    @Override
    public void showPersonalObjectives(List<Objective> objectives) {
        recentCalls.add(new Pair<>("showPersonalObjectives", List.of(objectives)));
    }

    @Override
    public void showCommonObjectives(List<Objective> objectives) {
        recentCalls.add(new Pair<>("showCommonObjectives", List.of(objectives)));
    }

    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        recentCalls.add(new Pair<>("updateDecks", List.of(drawableCards, numberOfCardsLeft)));
    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {
        recentCalls.add(new Pair<>("revealFinalSummary", List.of(nickname, objectivePoints, finalScore)));
    }

    @Override
    public void revealWinners(List<String> winners) {
        recentCalls.add(new Pair<>("revealWinners", List.of(winners)));
    }

    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color) {
        recentCalls.add(new Pair<>("notifyRemotePlayerDisconnected", List.of(nickname, color)));
    }

    @Override
    public void notifyPlayerLeftLobby(String nickname, Content color) {
        recentCalls.add(new Pair<>("notifyPlayerLeftLobby", List.of(nickname, color)));
    }

    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        recentCalls.add(new Pair<>("notifyRemotePlayerReconnected", List.of(nickname)));
    }

    @Override
    public void notifyGameTimeout() {
        recentCalls.add(new Pair<>("notifyGameTimeout", new ArrayList<>()));
    }

    @Override
    public void notifyGameCanceled() {
        recentCalls.add(new Pair<>("notifyGameCanceled", new ArrayList<>()));
    }

    @Override
    public void notifyTurnSkipped() {
        recentCalls.add(new Pair<>("notifyTurnSkipped", new ArrayList<>()));
    }

    @Override
    public void showNoMovesAvailable(String nickname) {
        recentCalls.add(new Pair<>("showNoMovesAvailable", List.of(nickname)));
    }

    @Override
    public void updateMatchList(List<GameInfo> matchList) {
        recentCalls.add(new Pair<>("updateMatchList", List.of(matchList)));
    }

    @Override
    public void newGameSuccess(int gameId) {
        recentCalls.add(new Pair<>("newGameSuccess", List.of(gameId)));
    }

    @Override
    public void showCriticalError(String message) {
        recentCalls.add(new Pair<>("showCriticalError", List.of(message)));
    }

    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId) {
        recentCalls.add(new Pair<>("showJoinGameDialog", List.of(colors, gameId)));
    }

    @Override
    public void showUserError(String message, int gameId) {
        recentCalls.add(new Pair<>("showUserError", List.of(message, gameId)));
    }

    @Override
    public void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers) {
        recentCalls.add(new Pair<>("showSuccessfulJoin", List.of(nickname, color, numberOfPlayers)));
    }

    @Override
    public void showReconnectionError(String message) {
        recentCalls.add(new Pair<>("showReconnectionError", List.of(message)));
    }

    @Override
    public void showDisconnectionMessage() {
        recentCalls.add(new Pair<>("showDisconnectionMessage", new ArrayList<>()));
    }

    public static void checkForUpdate(List<Pair<String, List<Object>>> recentCalls, String methodName, List<Object> args){
        Pair<String, List<Object>> call = recentCalls.getFirst();
        assertEquals(methodName, call.getKey());
        assertEquals(args, call.getValue());
    }
}