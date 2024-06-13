package it.polimi.ingsw;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TestView implements SetupView, GameView {
    private Map<Method, List<Object>> recentCalls = new LinkedHashMap<>();

    public Map<Method, List<Object>> getRecentCalls() {
        Map<Method, List<Object>> methods = new LinkedHashMap<>(recentCalls);
        recentCalls = new LinkedHashMap<>();
        return methods;
    }

    @Override
    public void notifyLastTurn() {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyLastTurn"), new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestDraw(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("requestDraw", Map.class, Map.class),
                    List.of(drawableCards, numberOfCardsLeft));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showChatMessage(ChatMessage chatMessage) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showChatMessage", ChatMessage.class),
                    List.of(chatMessage));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestPlacement(List<CardSides> handCards, List<BasicCard> placedCards) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("requestPlacement", List.class, List.class),
                    List.of(handCards, placedCards));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void turnChanged(String turnOwner) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("turnChanged", String.class),
                    List.of(turnOwner));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showErrorMessage(String message) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showErrorMessage", String.class),
                    List.of(message));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showUserJoined(String nickname, Content color, boolean isGameFull) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showUserJoined", String.class, Content.class, boolean.class),
                    List.of(nickname, color, isGameFull));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateRemotePlayerHand(String nickname, List<BasicCard> handCards) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("updateRemotePlayerHand", String.class, List.class),
                    List.of(nickname, handCards));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateLocalPlayerHand(List<CardSides> handCards) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("updateLocalPlayerHand", List.class),
                    List.of(handCards));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestStarterSide(List<CardSides> playerCards) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("requestStarterSide", List.class),
                    List.of(playerCards));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateBoard(String nickname, List<BasicCard> placedCards, int score) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("updateBoard", String.class, List.class, int.class),
                    List.of(nickname, placedCards, score));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requestPersonalObjectivesChoice(List<Objective> objectives) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("requestPersonalObjectivesChoice", List.class),
                    List.of(objectives));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showPersonalObjectives(List<Objective> objectives) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showPersonalObjectives", List.class),
                    List.of(objectives));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showCommonObjectives(List<Objective> objectives) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showCommonObjectives", List.class),
                    List.of(objectives));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType, Integer> numberOfCardsLeft) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("updateDecks", Map.class, Map.class),
                    List.of(drawableCards, numberOfCardsLeft));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void revealFinalSummary(String nickname, Map<Objective, Integer> objectivePoints, int finalScore) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("revealFinalSummary", String.class, Map.class, int.class),
                    List.of(nickname, objectivePoints, finalScore));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void revealWinners(List<String> winners) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("revealWinners", List.class),
                    List.of(winners));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyRemotePlayerDisconnected(String nickname, Content color) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyRemotePlayerDisconnected", String.class, Content.class),
                    List.of(nickname, color));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyPlayerLeftLobby(String nickname, Content color) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyPlayerLeftLobby", String.class, Content.class),
                    List.of(nickname, color));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyRemotePlayerReconnected(String nickname) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyRemotePlayerReconnected", String.class),
                    List.of(nickname));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyGameTimeout() {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyGameTimeout"),
                    new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyGameCanceled() {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyGameCanceled"),
                    new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void notifyTurnSkipped() {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("notifyTurnSkipped"),
                    new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showNoMovesAvailable(String nickname) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showNoMovesAvailable", String.class),
                    List.of(nickname));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void updateMatchList(List<GameInfo> matchList) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("updateMatchList", List.class),
                    List.of(matchList));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void newGameSuccess(int gameId) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("newGameSuccess", int.class),
                    List.of(gameId));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showCriticalError(String message) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showCriticalError", String.class),
                    List.of(message));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showJoinGameDialog", List.class, int.class),
                    new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showUserError(String message, int gameId) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showUserError", String.class, int.class),
                    List.of(message, gameId));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showSuccessfulJoin", String.class, Content.class, int.class),
                    List.of(nickname, color, numberOfPlayers));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showReconnectionError(String message) {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showReconnectionError", String.class),
                    List.of(message));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void showDisconnectionMessage() {
        try {
            recentCalls.put(TestView.class.getDeclaredMethod("showDisconnectionMessage"),
                    new ArrayList<>());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}