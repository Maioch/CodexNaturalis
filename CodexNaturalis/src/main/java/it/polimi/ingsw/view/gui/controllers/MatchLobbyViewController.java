package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;

import java.rmi.server.UnicastRemoteObject;
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
        playerRadioButton.setUserData(player);
        addRows(playerList, new ArrayList<>(List.of(new GridEntry(100, playerRadioButton))));
    }

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

    public void leaveLobby(){
        System.out.println("Leaving lobby");
        controller.sendMessage(new Message(Status.PLAYER_DISCONNECTED));
        controller.backToSetup();
    }
}