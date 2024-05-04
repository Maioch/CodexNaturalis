package it.polimi.ingsw.view;

import java.util.ArrayList;
import java.util.HashMap;

import it.polimi.ingsw.model.server.Content;

public interface SetupView {
    void updateMatchList(HashMap<Integer,String> matchList);
    void newGameSuccess(int gameId);
    void showCriticalError(String message);
    void showJoinGameDialog(ArrayList<Content> colors);
    void showUserError(String message);
    void showSuccessfulJoin();
}