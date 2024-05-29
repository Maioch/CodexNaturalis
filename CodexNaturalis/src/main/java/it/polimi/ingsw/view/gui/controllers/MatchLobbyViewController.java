package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

public class MatchLobbyViewController extends ViewController {
    @FXML
    public Label playerCountText;
    @FXML
    public GridPane playerList;

    private int currentNumberOfPlayers;
    private int maxNumberOfPlayers;

    public void initializeLabel(int numberOfPlayers){
        currentNumberOfPlayers = 1;
        maxNumberOfPlayers = numberOfPlayers;
        playerCountText.setText(String.format("Waiting for players... (1/%d)", numberOfPlayers));
    }

    /**
     * Updates the player count text. Adds the joined player's name.
     * @param player the newly joined player
     */
    public void updatePlayers(String player, Content color){
        currentNumberOfPlayers++;
        playerCountText.setText(String.format("Waiting for players... (%d/%d)", currentNumberOfPlayers, maxNumberOfPlayers));
    }
}