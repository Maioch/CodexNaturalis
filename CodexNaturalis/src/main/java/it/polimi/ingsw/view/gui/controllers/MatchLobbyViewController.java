package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

public class MatchLobbyViewController extends ViewController {
    @FXML
    public Label playerCountText;
    @FXML
    public GridPane playerList;

    private final ToggleGroup group = new ToggleGroup();
    private int currentNumberOfPlayers;
    private int maxNumberOfPlayers;

    /**
     * Initializes the player joined counting label on the lobby.
     *
     * @param numberOfPlayers the number of current joined player.
     */
    public void initializeLabel(String player, Content color, int numberOfPlayers){
        currentNumberOfPlayers = 0;
        maxNumberOfPlayers = numberOfPlayers;
        updatePlayers(player, color);
    }

    /**
     * Updates the player count text. Adds the joined player's name.
     *
     * @param player the newly joined player.
     */
    public void updatePlayers(String player, Content color){
        currentNumberOfPlayers++;
        playerCountText.setText(String.format("Waiting for players... (%d/%d)", currentNumberOfPlayers, maxNumberOfPlayers));
        RadioButton playerRadioButton = createRadioButton(player, group, "playerEntry");
        playerRadioButton.setDisable(true);
        playerRadioButton.setStyle(String.format("-radio-color: %s;", color.getHexColorString()));
        playerRadioButton.setAlignment(Pos.CENTER_LEFT);
        addRows(playerList, new ArrayList<>(List.of(new GridEntry(100, playerRadioButton))));
    }
}