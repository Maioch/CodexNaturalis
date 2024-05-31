package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

import java.util.*;

public class GameViewController extends ViewController {
    @FXML
    public ScrollPane gameBoardScrollPane;
    @FXML
    public GridPane cardHandGrid;
    @FXML
    public GridPane playerTagGrid;
    @FXML
    public TextField chatTextBox;
    @FXML
    public SplitMenuButton chatSendButton;
    @FXML
    public VBox chatMessageBox;
    @FXML
    public GridPane resourceDeckGrid;
    @FXML
    public GridPane goldDeckGrid;

    private Map<String, Content> players;

    public void initializeScene(){
        int numberOfPlayers = controller.getNumberOfPlayers();
        players = controller.getPlayerColors();
        List<GridEntry> playerEntry = new ArrayList<>();
        int index = 0;
        for(String nickname : players.keySet()){
            chatSendButton.getContextMenu().getItems().add(index, new CheckMenuItem(nickname));
            playerEntry.add(new GridEntry(100, createPlayerTag(nickname, players.get(nickname))));
            index++;
        }
        addColumns(playerTagGrid, playerEntry);
    }

    public void showChatMessage(String sender, String message){
        Label senderLabel = new Label(sender);
        senderLabel.getStyleClass().add("chatUserLabel");
        senderLabel.setStyle(String.format("-radio-color: %s;", players.get(sender).getHexColorString()));
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("chatMessageLabel");
        chatMessageBox.getChildren().add(new Label(sender));
        chatMessageBox.getChildren().add(messageLabel);
    }

    @FXML
    public void checkForText(){
        chatSendButton.setDisable(chatTextBox.getText().isEmpty() || chatTextBox.getText().length() > GameParameters.getMaxChatMessageLength());
    }

    private Node createPlayerTag(String nickname, Content color) {
        GridPane playerTagGrid = new GridPane();
        playerTagGrid.getColumnConstraints().addAll(Arrays.asList(
                new ColumnConstraints(40),
                new ColumnConstraints(),
                new ColumnConstraints(40)));
        Circle playerTagCircle = new Circle();
        playerTagCircle.getStyleClass().add("playerTagCircle");
        playerTagCircle.setStyle(String.format("-radio-color: %s;", color.getHexColorString()));
        playerTagGrid.addColumn(0, playerTagCircle);
        Label nicknameLabel = new Label(nickname);
        nicknameLabel.getStyleClass().add("playerTagText");
        playerTagGrid.addColumn(1, nicknameLabel);
        ImageView playerTagImageView = new ImageView();
        boolean isLocalPlayer = controller.getLocalPlayerName().equals(nickname);
        playerTagImageView.getStyleClass().add(isLocalPlayer ? "viewObjectivesIcon" : "viewBoardIcon");
        /*playerTagImageView.setOnMouseClicked(isLocalPlayer ? () -> {

        } : () -> {

        });*/
        return new RadioButton();
    }
}