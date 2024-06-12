package it.polimi.ingsw.view;

import java.util.List;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.model.server.Content;

public interface SetupView extends ReconnectableView{
    void updateMatchList(List<GameInfo> matchList);
    void newGameSuccess(int gameId);
    void showCriticalError(String message);
    void showJoinGameDialog(List<Content> colors, int gameId);
    void showUserError(String message, int gameId);
    void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers);
    void showReconnectionError(String message);
}