package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Class used to handle the match lobby scene of the GUI.
 */
public class MatchLobbyViewController extends ViewController {
    @FXML
    public Label playerCountText;
    @FXML
    public GridPane playerList;
    @FXML
    public GridPane disconnectionPopupGrid;
    @FXML
    public Label disconnectionLabel;
    @FXML
    public Button disconnectionButton;

    //the group that all the player labels are part of.
    private final ToggleGroup group = new ToggleGroup();

    //the number of players that are currently in the lobby.
    private int currentNumberOfPlayers;

    //the number of players required to start the game.
    private int maxNumberOfPlayers;

    public void initialize(){
        setDisconnectionControls(new DisconnectionControls(disconnectionPopupGrid, disconnectionLabel, disconnectionButton));
    }

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
        playerRadioButton.setUserData(player);
        addRows(playerList, new ArrayList<>(List.of(new GridEntry(100, playerRadioButton))));
    }

    /**
     * Removes a player from the lobby (updating the player count text).
     *
     * @param playerToRemove the nickname of the player to remove.
     */
    public void removePlayer(String playerToRemove){
        List<Node> playerTags = new ArrayList<>(playerList.getChildren());
        playerList.getChildren().clear();
        playerList.getRowConstraints().clear();
        currentNumberOfPlayers -= playerTags.removeIf(n -> n.getUserData().equals(playerToRemove)) ? 1 : 0;
        playerCountText.setText(String.format("Waiting for players... (%d/%d)", currentNumberOfPlayers, maxNumberOfPlayers));
        for (Node node : playerTags) {
            addRows(playerList, new ArrayList<>(List.of(new GridEntry(100, node))));
        }
    }

    /**
     * Lets the player leave the lobby.
     */
    public void leaveLobby(){
        client.getController().sendMessage(new Message(Status.PLAYER_DISCONNECTED));
    }
}