package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardSides;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.CardPlacementMessage;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.ObjectivesMessage;
import it.polimi.ingsw.view.gui.CardAssetsProvider;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.Timer;

public class GameViewController extends ViewController {
    @FXML
    public ScrollPane gameBoardScrollPane;
    @FXML
    public GridPane cardHandGrid;
    @FXML
    public GridPane starterChoicePopUp;
    @FXML
    public GridPane frontStarterSidePane;
    @FXML
    public GridPane backStarterSidePane;
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
    @FXML
    public ImageView objectivesIcon;
    @FXML
    public GridPane objectivesPane;
    @FXML
    public GridPane commonObjectivesGrid1;
    @FXML
    public GridPane commonObjectivesGrid2;
    @FXML
    public GridPane secretObjectiveGrid;
    @FXML
    public GridPane objectivesRevealPopUp;
    @FXML
    public GridPane commonObjectivesRevealPane;
    @FXML
    public GridPane secretObjectivesRevealPane;
    @FXML
    public GridPane objectivesButtonPane;
    @FXML
    public GridPane woodenPanePopUpBackground;

    private Map<String, Content> players;
    private final int toastGap = 76;

    /**
     * Initializes the game scene.
     */
    public void initializeScene(){
        players = controller.getPlayerColors();
        int index = 0;
        outerPlayerTagGrid.setVgap(16);
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

    /**
     * Notifies the players about the game's state when it changes.
     *
     * @param message the game's state.
     */
    public void updateStatusLabel(String message){
        Label statusLabel = new Label(message);
        statusLabel.getStyleClass().add("toastNotificationLabel");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setMaxHeight(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);
        int numberOfChildren = notificationToastGrid.getChildren().size();
        notificationToastGrid.getRowConstraints().add(new RowConstraints(70));
        notificationToastGrid.add(statusLabel, 1, 0);
        statusLabel.setTranslateY(numberOfChildren * toastGap);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    notificationToastGrid.getChildren().remove(statusLabel);
                    int i = 0;
                    for(Node node : notificationToastGrid.getChildren()){
                        node.setTranslateY(i * toastGap);
                        i++;
                    }
                });
            }
        }, 6 * 1000L);
    }

    /**
     * Prompts the client to choose the side of its starter to place.
     *
     * @param starterCard the starter card owned by the client.
     */
    public void chooseStarterSide(CardSides starterCard){
        ImageView frontSide = getCardImage(starterCard.frontSide());
        ImageView backSide = getCardImage(starterCard.backSide());
        frontSide.getStyleClass().add("cardWithShadow");
        backSide.getStyleClass().add("cardWithShadow");
        frontSide.setOnMouseClicked((mouseEvent -> {
            frontSide.setDisable(true);
            backSide.setDisable(true);
            starterChoicePopUp.setVisible(false);
            woodenPanePopUpBackground.setVisible(false);
            controller.sendMessage(new CardPlacementMessage(starterCard.frontSide(), null));
        }));
        frontStarterSidePane.add(backSide, 0, 0);
        backSide.setOnMouseClicked((mouseEvent -> {
            frontSide.setDisable(true);
            backSide.setDisable(true);
            starterChoicePopUp.setVisible(false);
            woodenPanePopUpBackground.setVisible(false);
            controller.sendMessage(new CardPlacementMessage(starterCard.backSide(), null));
        }));
        backStarterSidePane.add(frontSide, 0, 0);
        starterChoicePopUp.setVisible(true);
        woodenPanePopUpBackground.setVisible(true);
    }

    /**
     * Shows the objectives panel.
     */
    @FXML
    public void openObjectivesPane(){
        objectivesPane.setVisible(true);
        objectivesButtonPane.setVisible(false);
    }

    /**
     * Closes the objectives panel.
     */
    @FXML
    public void closeObjectivesPane(){
        objectivesButtonPane.setVisible(true);
        objectivesPane.setVisible(false);
    }

    /**
     * Updates the graphic representation of the client's common objectives.
     *
     * @param objective1 the client's first common objective.
     * @param objective2 the client's second common objective.
     */
    public void setCommonObjectives(Objective objective1, Objective objective2){
        commonObjectivesGrid1.add(getCardImage(objective1), 0, 0);
        commonObjectivesGrid2.add(getCardImage(objective2), 0, 0);
        commonObjectivesRevealPane.addColumn(0, getCardImage(objective1));
        commonObjectivesRevealPane.addColumn(1, getCardImage(objective2));
    }

    /**
     * Updates the graphic representation of the client's personal/secret objective.
     *
     * @param objective the client's secret objective.
     */
    public void setPersonalObjectives(Objective objective){
        secretObjectiveGrid.add(getCardImage(objective), 0, 0);
        objectivesIcon.setDisable(false);
        objectivesRevealPopUp.setVisible(false);
        woodenPanePopUpBackground.setVisible(false);
    }

    /**
     * Prompts the client to choose its personal/secret objective.
     *
     * @param objective1 the first objective option
     * @param objective2 the second objective option.
     */
    public void choosePersonalObjective(Objective objective1, Objective objective2){
        ImageView objectiveView1 = getCardImage(objective1);
        ImageView objectiveView2 = getCardImage(objective2);
        objectiveView1.setOnMouseClicked((mouseEvent) -> {
            objectiveView1.setDisable(true);
            objectiveView2.setDisable(true);
            objectivesRevealPopUp.setVisible(false);
            controller.sendMessage(new ObjectivesMessage(
                    Status.REQUEST_SECRET_OBJECTIVES, new ArrayList<>(List.of(objective1))));
        });
        objectiveView2.setOnMouseClicked((mouseEvent) -> {
            objectiveView1.setDisable(true);
            objectiveView2.setDisable(true);
            objectivesRevealPopUp.setVisible(false);
            controller.sendMessage(new ObjectivesMessage(
                    Status.REQUEST_SECRET_OBJECTIVES, new ArrayList<>(List.of(objective2))));
        });
        secretObjectivesRevealPane.addColumn(0, objectiveView1);
        secretObjectivesRevealPane.addColumn(1, objectiveView2);
        objectivesRevealPopUp.setVisible(true);
        woodenPanePopUpBackground.setVisible(true);
    }

    /**
     * Updates the cards held by the local player and shows the side popup when the player wants to place one.
     * In the popup both sides of the card are shown.
     *
     * @param cards the player's hand cards.
     */
    public void updateLocalPlayerCards(List<CardSides> cards) {
        if(!checkCurrentView(controller.getLocalPlayerName())){
            return;
        }
        int index = 0;
        for(CardSides card : cards) {
            ImageView cardView = getCardImage(card.frontSide());
            cardView.setOnMouseClicked((event) -> {
                //TODO
            });
            cardView.setDisable(true);
            cardHandGrid.add(cardView, index, 0);
            index++;
        }
    }

    /**
     * Updates the cards held by the specified remote player.
     *
     * @param nickname the remote player's nickname
     * @param cards    the player's hand cards.
     */
    public void updateRemotePlayerCards(String nickname, List<BasicCard> cards){
        if(!checkCurrentView(nickname)){
            return;
        }
        int index = 0;
        for(BasicCard card : cards) {
            cardHandGrid.add(getCardImage(card), index, 0);
            index++;
        }
    }

    /**
     * Updates the resource and gold decks.
     *
     * @param drawableCards a map containing the list of resource cards and the list of gold cards.
     *                      The first element of each list represents the card on top of the deck.
     *                      The rest are visible cards.
     */
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards){
        List<BasicCard> resourceCards = drawableCards.get(CardType.RESOURCE);
        List<BasicCard> goldCards = drawableCards.get(CardType.GOLD);
        int index = 0;
        for(BasicCard card : resourceCards){
            ImageView cardView = getCardImage(card);
            cardView.setDisable(true);
            cardView.setOnMouseClicked((mouseEvent) -> {
                //TODO
            });
            resourceDeckGrid.add(cardView, index, 0);
            index++;
        }
        index = 0;
        for(BasicCard card : goldCards){
            ImageView cardView = getCardImage(card);
            cardView.setDisable(true);
            cardView.setOnMouseClicked((mouseEvent) -> {
                //TODO
            });
            goldDeckGrid.add(cardView, index, 0);
            index++;
        }
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

        if(!controller.getLocalPlayerName().equals(nickname)) {
            playerTagImageView.getStyleClass().add("viewBoardIcon");
            playerTagImageView.getStyleClass().add("playerTagIcon");
            playerTagImageView.setPickOnBounds(true);
            playerTagImageView.setOnMouseClicked((mouseEvent) -> {

            });
            playerTagGrid.addColumn(2, playerTagImageView);
        }
        return playerTagGrid;
    }

    /**
     * Crates an ImageView containing a card side with the needed measures.
     *
     * @param card the card to represent.
     * @return     the ImageView containing the card.
     */
    private ImageView getCardImage(BasicCard card){
        ImageView cardView = new ImageView(CardAssetsProvider.getCardFilePath(card));
        cardView.setFitWidth(130);
        cardView.setPreserveRatio(true);
        return cardView;
    }

    /**
     * Crates an ImageView containing an objective with the needed measures.
     *
     * @param objective the objective to represent.
     * @return          the ImageView containing the objective.
     */
    private ImageView getCardImage(Objective objective) {
        ImageView objectiveView = new ImageView(CardAssetsProvider.getObjectiveFilePath(objective));
        objectiveView.setFitWidth(130);
        objectiveView.setPreserveRatio(true);
        return objectiveView;
    }

    private boolean checkCurrentView(String nickname) {
        //TODO
        return true;
    }
}