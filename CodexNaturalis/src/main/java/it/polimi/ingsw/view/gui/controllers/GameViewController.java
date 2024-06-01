package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.*;

public class GameViewController extends ViewController {
    @FXML
    public ScrollPane gameBoardScrollPane;
    @FXML
    public GridPane cardHandGrid;
    @FXML
    public GridPane playersTagsGrid;
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
    @FXML
    public GridPane outerPlayerTagGrid;
    @FXML
    public GridPane notificationToastGrid;

    private Map<String, Content> players;

    /**
     * Initializes the game scene.
     */
    public void initializeScene(){
        players = controller.getPlayerColors();
        int index = 0;
        for(String nickname : players.keySet()){
            if(!nickname.equals(controller.getLocalPlayerName())) {
                chatSendButton.getItems().add(new CheckMenuItem(nickname));
            }
            GridPane playerGrid = createPlayerTag(nickname, players.get(nickname));
            playersTagsGrid.add(playerGrid, index, 0);
            playersTagsGrid.getColumnConstraints().add(index, new ColumnConstraints(playerGrid.getPrefWidth()));
            index++;
        }
    }

    /**
     * Shows a new chat message.
     *
     * @param sender  the sender's nickname
     * @param message the message text content.
     */
    public void showChatMessage(String sender, String message){
        Label senderLabel = new Label(sender + ":");
        senderLabel.getStyleClass().add("chatUserLabel");
        senderLabel.setStyle(String.format("-user-color: %s;", players.get(sender).getHexColorString()));
        Label messageLabel = new Label(message);
        messageLabel.getStyleClass().add("chatMessageLabel");
        chatMessageBox.getChildren().add(senderLabel);
        chatMessageBox.getChildren().add(messageLabel);
    }

    /**
     * Checks if the chat textbox is empty or filled with a too long message (length set as a game parameter).
     * If that's true, the chat button is disabled.
     */
    @FXML
    public void checkForText(){
        chatSendButton.setDisable(chatTextBox.getText().isEmpty() || chatTextBox.getText().length() > GameParameters.getMaxChatMessageLength());
    }

    /**
     * Sends a previously written message to the specified players.
     */
    @FXML
    public void sendChatMessage() {
        String message = chatTextBox.getText();
        chatTextBox.setText("");
        chatSendButton.setDisable(true);
        List<String> recipients = new ArrayList<>(){{
            boolean nothingSelected = true;
            for(MenuItem menuItem : chatSendButton.getItems()){
                if(((CheckMenuItem) menuItem).isSelected()){
                    nothingSelected = false;
                    add(menuItem.getText());
                }
            }
            if(nothingSelected){
                addAll(controller.getRemotePlayerNames());
            }
        }};
        controller.sendMessage(new ChatMessage(message, null, recipients));
    }

    public void updateStatusLabel(String message){
        int lastRow = notificationToastGrid.getRowCount();
        Label statusLabel = new Label(message);
        statusLabel.getStyleClass().add("toastNotificationLabel");
        notificationToastGrid.addRow(lastRow, statusLabel );
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    notificationToastGrid.getChildren().remove(statusLabel);
                });
            }
        }, 6 * 1000L);
    }

    /**
     * Creates a player tag scene element. Used to show the players.
     * When one of these elements is clicked, the game will show the selected player's current board.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     * @return         the created "tag" node.
     */
    private GridPane createPlayerTag(String nickname, Content color) {
        GridPane playerTagGrid = new GridPane();
        playerTagGrid.getRowConstraints().add(new RowConstraints(outerPlayerTagGrid.getPrefHeight()));
        GridPane.setValignment(playerTagGrid, VPos.CENTER);
        playerTagGrid.getColumnConstraints().addAll(Arrays.asList(
                new ColumnConstraints(40),
                new ColumnConstraints(100),
                new ColumnConstraints(40)));
        GridPane.setMargin(playerTagGrid, new Insets(0, 16, 0, 16));
        playerTagGrid.getStyleClass().add("playerTagGrid");
        playerTagGrid.setPrefHeight(playersTagsGrid.getPrefHeight());

        //adds the player's color(ed circle)
        Circle playerTagCircle = new Circle(12);
        playerTagCircle.getStyleClass().add("playerTagCircle");
        GridPane.setHalignment(playerTagCircle, HPos.CENTER);
        playerTagCircle.setStyle(String.format("-circle-color: %s;", color.getHexColorString()));
        playerTagGrid.addColumn(0, playerTagCircle);

        //adds the player's nickname label
        Label nicknameLabel = new Label(nickname);
        nicknameLabel.setPrefWidth(100);
        nicknameLabel.setPrefHeight(24);
        nicknameLabel.setTranslateY(-1);
        nicknameLabel.getStyleClass().add("playerTagText");
        playerTagGrid.addColumn(1, nicknameLabel);

        //adds the player's tag image
        ImageView playerTagImageView = new ImageView();
        playerTagImageView.setPreserveRatio(true);
        playerTagImageView.setFitWidth(24);
        GridPane.setHalignment(playerTagImageView, HPos.CENTER);

        boolean isLocalPlayer = controller.getLocalPlayerName().equals(nickname);
        playerTagImageView.getStyleClass().add(isLocalPlayer ? "viewObjectivesIcon" : "viewBoardIcon");
        playerTagImageView.setOnMouseClicked(isLocalPlayer ? (mouseEvent) -> {

        } : (mouseEvent) -> {

        });
        playerTagGrid.addColumn(2, playerTagImageView);
        return playerTagGrid;
    }
}