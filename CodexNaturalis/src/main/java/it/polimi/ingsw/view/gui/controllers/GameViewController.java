package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.*;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.game.CardPlacementMessage;
import it.polimi.ingsw.network.messages.game.ChatMessage;
import it.polimi.ingsw.network.messages.game.DrawChoiceMessage;
import it.polimi.ingsw.network.messages.game.ObjectivesMessage;
import it.polimi.ingsw.view.gui.CardAssetsProvider;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

import java.util.*;
import java.util.Timer;

@SuppressWarnings("FieldCanBeLocal")
public class GameViewController extends ViewController {
    @FXML
    public ScrollPane gameBoardScrollPane;
    @FXML
    public Pane gameBoardPane;
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
    @FXML
    public GridPane cardSelectionPopup;
    @FXML
    public GridPane cardSelectionGrid;
    @FXML
    public Pane scorePane;
    @FXML
    public Pane dragLayerPane;

    private Map<String, Content> players;
    private String currentViewedPlayer;
    private final List<Label> currentPermanentMessages = new ArrayList<>();
    private final Map<String, Circle> playerTagCircles = new HashMap<>();
    private final Map<String, ImageView> playerTagViewIcons = new HashMap<>();
    private final Map<String, ImageView> playerTokens = new HashMap<>();

    private final int toastGap = 76;
    private final int maxNumberOfHiddenCards = 3;
    private final int distanceBetweenHiddenCards = 4;
    private final int distanceBetweenTokens = 4;
    private final long statusLabelShowInterval = 6;
    private final int cardWidth = 150;
    private final int objectiveWidth = 130;

    /**
     * Initializes the game scene.
     */
    public void initializeScene(){
        players = controller.getPlayerColors();
        currentViewedPlayer = controller.getLocalPlayerName();
        int index = 0;
        outerPlayerTagGrid.setVgap(16);
        /*ImageView card = getCardImage(CardBuilder.buildCard(82).frontSide());
        gameBoardPane.getChildren().add(card);
        card.setX(0);
        card.setY(0);*/
        for(String nickname : players.keySet()){
            if(!nickname.equals(controller.getLocalPlayerName())) {
                chatSendButton.getItems().add(new CheckMenuItem(nickname));
            }
            GridPane playerGrid = createPlayerTag(nickname, players.get(nickname));
            playersTagsGrid.add(playerGrid, index, 0);
            playersTagsGrid.getColumnConstraints().add(index, new ColumnConstraints(playerGrid.getPrefWidth()));
            ImageView playerToken = new ImageView();
            playerToken.setFitWidth(30);
            playerToken.setPreserveRatio(true);
            playerToken.getStyleClass().add(players.get(nickname).name().toLowerCase() + "Token");
            playerTokens.put(nickname, playerToken);
            ((GridPane)scorePane.getChildren().getLast()).add(playerToken, 0, 0);
            updateScore(nickname, 0);
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
     * Method that set the disable field of the chat components. It will not enable the chat button.
     *
     * @param disable the boolean to set.
     */
    public void setChatDisable(boolean disable){
        if(disable){
            chatSendButton.setDisable(true);
        }
        chatTextBox.setDisable(disable);
    }

    /**
     * Notifies the players about the game's state when it changes.
     *
     * @param message the game's state.
     */
    public void updateStatusLabel(String message){
        Label statusLabel = createStatusLabel(message);
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> removeMessageLabel(statusLabel));
            }
        }, statusLabelShowInterval * 1000);
    }

    /**
     * Notifies the players about the game's state when it changes.
     * The message will not disappear in a set amount of time.
     *
     * @param message     the game's state.
     * @param messageType the type of the message.
     */
    public void updateStatusLabel(String message, String messageType){
        Label statusLabel = createStatusLabel(message);
        statusLabel.setUserData(messageType);
        currentPermanentMessages.add(statusLabel);
    }

    /**
     * Removes the current status labels that have the supplied messageType.
     *
     * @param messageType the message type.
     */
    public void hideStatusLabel(String messageType){
        for(Label label : currentPermanentMessages.stream().filter(l -> l.getUserData().equals(messageType)).toList()){
            removeMessageLabel(label);
        }
        currentPermanentMessages.removeIf(l -> l.getUserData().equals(messageType));
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
        cardHandGrid.getChildren().removeIf(c -> !(c == cardSelectionPopup));
        int index = 0;
        for(CardSides card : cards.stream().limit(3).toList()) {
            ImageView cardView = getCardImage(card.frontSide());
            int finalIndex = index;
            cardView.setOnMouseClicked((event) -> {
                if(!cardSelectionPopup.isVisible() || GridPane.getColumnIndex(cardSelectionPopup) != finalIndex) {
                    ImageView frontView = createDraggableCard(card.frontSide());
                    ImageView backView = createDraggableCard(card.backSide());
                    cardSelectionGrid.getChildren().clear();
                    cardSelectionGrid.add(frontView, 0, 0);
                    cardSelectionGrid.add(backView, 0, 1);
                    GridPane.setColumnIndex(cardSelectionPopup, finalIndex);
                    cardSelectionPopup.setVisible(true);
                } else {
                    cardSelectionPopup.setVisible(false);
                }
            });
            cardView.setDisable(!controller.getLocalPlayerName().equals(controller.getPlayerWithTurn()));
            cardHandGrid.add(cardView, index, 0);
            index++;
        }
    }

    private ImageView createDraggableCard(BasicCard card) {
        ImageView view = getCardImage(card);
        ImageView draggableCard = getCardImage(card);
        view.setOnDragDetected((MouseEvent e) -> {
            dragLayerPane.getChildren().add(draggableCard);
            cardSelectionPopup.setVisible(false);
        });
        /*view.setOnMouseDragExited((MouseEvent e) -> System.out.println("DRAXI"));
        view.setOnMouseDragReleased((MouseEvent e) -> System.out.println("DRAVO"));
        view.setOnMouseDragOver((MouseEvent e) -> System.out.println("DROVE"));*/
        view.setOnMouseDragged((MouseEvent e) -> {
            draggableCard.setLayoutX(e.getSceneX() - draggableCard.getFitWidth() / 2);
            draggableCard.setLayoutY(e.getSceneY() - draggableCard.getLayoutBounds().getHeight() / 2);
        });
        return view;
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
        cardHandGrid.getChildren().removeIf(c -> !(c == cardSelectionPopup));
        cardSelectionPopup.setVisible(false);
        int index = 0;
        for(BasicCard card : cards.stream().limit(3).toList()) {
            cardHandGrid.add(getCardImage(card), index, 0);
            index++;
        }
    }

    /**
     * Places a card.
     *
     * @param handCards the local player's hand cards.
     * @param placedCards the local player's board.
     */
    public void placeCard(List<CardSides> handCards, List<BasicCard> placedCards){
        updateLocalPlayerCards(handCards);
        updateLocalPlayerBoard(placedCards);
    }

    /**
     * Updates the score board, by positioning at the correct position the player's token.
     *
     * @param nickname the nickname of the player of which the score has to be updated.
     * @param playerScore the new player's score.
     */
    public void updateScore(String nickname, int playerScore){
        if(playerScore >= 30){
            playerScore = 29;
        }
        ImageView playerToken = playerTokens.get(nickname);
        GridPane gridPaneToAddTo = (GridPane)scorePane.getChildren().get(playerScore);
        GridPane gridPaneToRemoveFrom = (GridPane)playerToken.getParent();
        if(gridPaneToAddTo == gridPaneToRemoveFrom){
            return;
        }
        gridPaneToRemoveFrom.getChildren().remove(playerToken);
        for(Node node : gridPaneToRemoveFrom.getChildren()){
            double tokenY = node.getTranslateY();
            if(tokenY < playerToken.getTranslateY()){
                node.setTranslateY(tokenY + distanceBetweenTokens);
            }
        }
        gridPaneToAddTo.add(playerToken, 0, 0);
        playerToken.setTranslateY(-distanceBetweenTokens * (gridPaneToAddTo.getChildren().size() - 1));
    }

    /**
     * Updates the resource and gold decks.
     *
     * @param drawableCards     a map containing the list of resource cards and the list of gold cards.
     *                          The first element of each list represents the card on top of the deck.
     *                          The rest are visible cards.
     * @param numberOfCardsLeft a map containing the number of cards left of each deck.
     */
    public void updateDecks(Map<CardType, List<BasicCard>> drawableCards, Map<CardType,Integer> numberOfCardsLeft){
        CardType resource = CardType.RESOURCE;
        CardType gold = CardType.GOLD;
        updateViewDeck(drawableCards.get(resource), numberOfCardsLeft.get(resource), resource);
        updateViewDeck(drawableCards.get(gold), numberOfCardsLeft.get(gold), gold);
    }

    /**
     * Updates the local player board, by visually placing the cards.
     * 
     * @param placedCards the local player's board.
     */
    public void updateLocalPlayerBoard(List<BasicCard> placedCards){
        /*gameBoardPane.getChildren().clear();
        for(BasicCard card : placedCards) {
            GridPane cardGrid = new GridPane();
            cardGrid.getStyleClass().add("boardCard");
            cardGrid.setStyle(String.format("-card-asset: %s", CardAssetsProvider.getCardFilePath(card)));

            //TODO

            gameBoardPane.getChildren().add(cardGrid);
        }*/
    }

    public void updateRemotePlayerBoard(String nickname, List<BasicCard> placedCards){

    }

    /**
     * Sets the current turn owner.
     *
     * @param turnOwner the turn-owner's nickname.
     */
    public void setCurrentTurnOwner(String turnOwner){
        for(Circle circle : playerTagCircles.values()){
            circle.setStroke(Paint.valueOf("transparent"));
        }
        playerTagCircles.get(turnOwner).setStroke(Paint.valueOf("white"));
    }

    /**
     * Creates the status label.
     *
     * @param message the message written in the label.
     * @return the created label.
     */
    private Label createStatusLabel(String message){
        Label statusLabel = new Label(message);
        statusLabel.getStyleClass().add("toastNotificationLabel");
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        statusLabel.setMaxHeight(Double.MAX_VALUE);
        statusLabel.setAlignment(Pos.CENTER);
        int numberOfChildren = notificationToastGrid.getChildren().size();
        notificationToastGrid.add(statusLabel, 1, 0);
        statusLabel.setTranslateY(numberOfChildren * toastGap);
        return statusLabel;
    }

    /**
     * Helper method that updates the view of a specified deck.
     *
     * @param cards             the list of card in the deck
     * @param numberOfCardsLeft the number of card left
     * @param deckType          the type of the deck
     */
    private void updateViewDeck(List<BasicCard> cards, int numberOfCardsLeft, CardType deckType){
        GridPane deck = deckType == CardType.RESOURCE ? resourceDeckGrid : goldDeckGrid;
        int index = 0;
        for(BasicCard card : cards){
            ImageView cardView = getCardImage(card);
            cardView.setDisable(true);
            int finalIndex = index;
            cardView.setOnMouseClicked((mouseEvent) -> controller.sendMessage(new DrawChoiceMessage(finalIndex, deckType)));
            cardView.setOnMouseClicked((mouseEvent) -> controller.sendMessage(new DrawChoiceMessage(finalIndex, deckType)));
            if(index == 0){
                int j = 0;
                while(j < numberOfCardsLeft && j < maxNumberOfHiddenCards){
                    ImageView hiddenCardView = createHiddenCard();
                    deck.add(hiddenCardView, index, 0);
                    hiddenCardView.setTranslateY(-distanceBetweenHiddenCards * j);
                    j++;
                }
                deck.add(cardView, index, 0);
                cardView.setTranslateY(-distanceBetweenHiddenCards * j);
            }else{
                deck.add(cardView, index, 0);
            }
            index++;
        }
    }

    /**
     * Helper method that removes the given label from the notification toast.
     *
     * @param label the label to remove.
     */
    private void removeMessageLabel(Label label){
        notificationToastGrid.getChildren().remove(label);
        int i = 0;
        for(Node node : notificationToastGrid.getChildren()){
            node.setTranslateY(i * toastGap);
            i++;
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
        playerTagCircle.setStrokeWidth(3);
        playerTagCircle.setStroke(Paint.valueOf("transparent"));
        playerTagGrid.addColumn(0, playerTagCircle);
        playerTagCircles.put(nickname, playerTagCircle);

        //adds the player's nickname label
        Label nicknameLabel = new Label(nickname);
        nicknameLabel.setPrefWidth(100);
        nicknameLabel.setPrefHeight(24);
        nicknameLabel.setTranslateY(-1);
        nicknameLabel.getStyleClass().add("playerTagText");
        playerTagGrid.addColumn(1, nicknameLabel);

        //adds the icon that, when clicked, lets the player
        //see the opponent's board and the back of their hand
        if(!controller.getLocalPlayerName().equals(nickname)) {
            ImageView viewPlayerIcon = new ImageView();
            viewPlayerIcon.setPreserveRatio(true);
            viewPlayerIcon.setFitWidth(24);
            GridPane.setHalignment(viewPlayerIcon, HPos.CENTER);
            viewPlayerIcon.getStyleClass().add("viewBoardIcon");
            viewPlayerIcon.getStyleClass().add("playerTagIcon");
            viewPlayerIcon.setPickOnBounds(true);
            playerTagViewIcons.put(nickname, viewPlayerIcon);
            viewPlayerIcon.setOnMouseClicked((mouseEvent) -> {
                if(checkCurrentView(nickname)) {
                    currentViewedPlayer = controller.getLocalPlayerName();
                    updateLocalPlayerCards(controller.getLocalPlayerHand());
                    updateLocalPlayerBoard(controller.getLocalPlayerBoard());
                    viewPlayerIcon.getStyleClass().remove("hideBoardIcon");
                    viewPlayerIcon.getStyleClass().add("viewBoardIcon");
                } else {
                    currentViewedPlayer = nickname;
                    updateRemotePlayerCards(nickname, controller.getRemotePlayerHand(nickname));
                    updateRemotePlayerBoard(nickname, controller.getRemotePlayerBoard(nickname));
                    for(ImageView icon : playerTagViewIcons.values()){
                        icon.getStyleClass().remove("hideBoardIcon");
                        viewPlayerIcon.getStyleClass().add("viewBoardIcon");
                    }
                    viewPlayerIcon.getStyleClass().remove("viewBoardIcon");
                    viewPlayerIcon.getStyleClass().add("hideBoardIcon");
                }
            });
            playerTagGrid.addColumn(2, viewPlayerIcon);
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
        cardView.setFitWidth(cardWidth);
        cardView.getStyleClass().add("cardWithShadow");
        cardView.setPreserveRatio(true);
        return cardView;
    }

    /**
     * Creates the ImageView of a hidden card.
     *
     * @return the created ImageView.
     */
    private ImageView createHiddenCard(){
        ImageView cardView = new ImageView(CardAssetsProvider.getHiddenCardFilePath());
        cardView.setFitWidth(cardWidth);
        cardView.getStyleClass().add("cardWithShadow");
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
        objectiveView.setFitWidth(objectiveWidth);
        objectiveView.setPreserveRatio(true);
        return objectiveView;
    }

    /**
     * Checks if the given player is the one currently viewed.
     *
     * @param nickname the player name
     * @return         true if the given player is the one currently viewed.
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkCurrentView(String nickname) {
        return nickname.equals(currentViewedPlayer);
    }
}