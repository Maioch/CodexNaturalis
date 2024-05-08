package it.polimi.ingsw.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.polimi.ingsw.model.server.Content;

public interface SetupView {
    void updateMatchList(Map<Integer,String> matchList);
    void newGameSuccess(int gameId);
    void showCriticalError(String message);
    void showJoinGameDialog(List<Content> colors, int gameId);
    void showUserError(String message, int gameId);
    void showSuccessfulJoin();
}