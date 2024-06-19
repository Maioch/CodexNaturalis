package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.controller.server.GameInfo;
import it.polimi.ingsw.controller.server.GameStatus;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.GameParameters;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.shared.messages.setup.NewGameMessage;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class used to handle the march browsing scene of the GUI.
 */
public class MatchBrowserViewController extends ViewController {
    @FXML
    public Button requestCreationButton;
    @FXML
    public GridPane matchGridPane;
    @FXML
    public GridPane errorPopupGrid;
    @FXML
    public Label errorLabel;
    @FXML
    public Button okButton;
    @FXML
    public GridPane joinPopupGrid;
    @FXML
    public TextField nicknameTextBox;
    @FXML
    public Button joinPopupButton;
    @FXML
    public Button refreshButton;
    @FXML
    public Button requestJoinButton;
    @FXML
    public GridPane colorChoiceGrid;
    @FXML
    public GridPane createPopupGrid;
    @FXML
    public TextField matchNameTextbox;
    @FXML
    public Button createPopupButton;
    @FXML
    public ImageView backFromCreateIcon;
    @FXML
    public ImageView backFromJoinIcon;
    @FXML
    public ToggleGroup playerNumberToggleGroup;
    @FXML
    public ImageView backFromReconnectIcon;
    @FXML
    public Button reconnectButton;
    @FXML
    public TextField reconnectNicknameTextbox;
    @FXML
    public GridPane reconnectPopUp;
    @FXML
    public GridPane disconnectionPopupGrid;
    @FXML
    public Button disconnectionButton;
    @FXML
    public Label disconnectionLabel;

    private final List<List<RadioButton>> radioButtons = new ArrayList<>();
    private ToggleGroup gameIdToggleGroup;
    private ToggleGroup colorChoiceToggleGroup;
    private int currentSelectedId;
    private final double animationOffset = 200;

    public void initialize(){
        setDisconnectionControls(new DisconnectionControls(disconnectionPopupGrid, disconnectionLabel, disconnectionButton));
    }

    /**
     * Method that adds all matches to the match list.
     * @param matchList the list of matches.
     */
    public void addMatches(List<GameInfo> matchList){
        gameIdToggleGroup = new ToggleGroup();
        ToggleGroup groupName = new ToggleGroup();
        ToggleGroup groupStatus = new ToggleGroup();
        for(GameInfo match : matchList) {
            int id = match.getGameId();
            GridPane grid = new GridPane();
            RadioButton idRadioButton = createRadioButton(String.valueOf(id), gameIdToggleGroup, "tableButton");
            setupMatchListButton(idRadioButton);
            idRadioButton.setDisable(match.getGameStatus() == GameStatus.STARTED);
            idRadioButton.setId(String.valueOf(id));
            idRadioButton.setUserData(match.getGameStatus().getText());
            RadioButton nameRadioButton = createRadioButton(match.getGameName(), groupName, "tableButton");
            setupMatchListButton(nameRadioButton);
            nameRadioButton.setDisable(match.getGameStatus() == GameStatus.STARTED);
            RadioButton statusRadioButton = createRadioButton(match.getGameStatus().getText(), groupStatus, "tableButton");
            setupMatchListButton(statusRadioButton);
            statusRadioButton.setDisable(match.getGameStatus() == GameStatus.STARTED);
            radioButtons.add(Arrays.asList(idRadioButton, nameRadioButton, statusRadioButton));
            List<GridEntry> entries = new ArrayList<>(Arrays.asList(
                    new GridEntry(12, idRadioButton),
                    new GridEntry(53, nameRadioButton),
                    new GridEntry(35, statusRadioButton)
            ));
            addColumns(grid, entries);
            matchGridPane.addRow(matchGridPane.getRowCount(), grid);
        }
    }

    /**
     * Method used to add CSS effects and method callbacks to the matches list entries.
     * @param button the entry to stylize.
     */
    private void setupMatchListButton(RadioButton button){
        button.getStyleClass().remove("radio-button");
        button.setOnMouseClicked((mouseEvent) -> {
            requestJoinButton.setDisable(false);
            disableButtonRow(mouseEvent);
            setRadioButtonStyle(mouseEvent, "tableButton");
        });
        button.setOnMousePressed(mouseEvent -> setRadioButtonStyle(mouseEvent, "pressedTableButton"));
        button.setOnMouseEntered(mouseEvent -> setRadioButtonStyle(mouseEvent, "hoveredTableButton"));
        button.setOnMouseExited(mouseEvent -> setRadioButtonStyle(mouseEvent, "tableButton"));
    }

    /**
     * Method that disables the newly selected row.
     * @param mouseEvent the event that causes the method run.
     */
    private void disableButtonRow(MouseEvent mouseEvent){
        RadioButton sender = (RadioButton) mouseEvent.getSource();
        List<RadioButton> currentRow = radioButtons.stream()
                .filter(l  -> l.stream().anyMatch(r -> r.getParent().equals(sender.getParent())))
                .findFirst().orElseThrow();
        for(RadioButton r : currentRow){
            r.setSelected(true);
        }
    }

    /**
     * Sets the radio button's CSS.
     *
     * @param mouseEvent the event causing the change of style
     * @param styleClass the desired style class
     */
    private void setRadioButtonStyle(MouseEvent mouseEvent, String styleClass){
        RadioButton sender = (RadioButton) mouseEvent.getSource();
        List<RadioButton> currentRow = radioButtons.stream()
                .filter(l  -> l.stream().anyMatch(r -> r.getParent().equals(sender.getParent())))
                .findFirst().orElseThrow();
        for(RadioButton r : currentRow){
            r.getStyleClass().clear();
            r.getStyleClass().add(styleClass);
        }
    }

    /**
     * Refreshes the match list (by requesting the games list).
     */
    @FXML
    public void refreshMatchList(){
        client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Sets the create-game pop-up to visible.
     */
    @FXML
    public void requestCreation(){
        joinPopupGrid.setVisible(false);
        createPopupGrid.setVisible(true);
        Animator.doFadeAnimation(createPopupGrid, true);
        Animator.doPopAnimation(createPopupGrid.getChildren().getFirst(), animationOffset, true);

    }

    /**
     * By firstly checking the create-input, enables the "create" button.
     */
    @FXML
    public void checkCreateInput(){
        String matchName = matchNameTextbox.getText();
        createPopupButton.setDisable(matchName.isEmpty() ||
                matchName.length() > GameParameters.getMaxNicknameLength() ||
                playerNumberToggleGroup.getSelectedToggle() == null);
    }

    /**
     * Tries to create a new game.
     */
    @FXML
    public void tryCreate(){
        createPopupButton.setDisable(true);
        matchNameTextbox.setDisable(true);
        for(Toggle toggle : playerNumberToggleGroup.getToggles()){
            if(toggle instanceof RadioButton radioButton){
                radioButton.setDisable(true);
            }
        }
        String matchName = matchNameTextbox.getText();
        int numberOfPlayers = Integer.parseInt(((RadioButton)playerNumberToggleGroup.getSelectedToggle()).getId());
        client.getController().sendMessage(new NewGameMessage(matchName, numberOfPlayers));
    }

    /**
     * Starts the game joining procedure (by sending a "REQUEST_COLORS" message).
     */
    @FXML
    public void requestJoin(){
        boolean isReconnect = ((RadioButton) gameIdToggleGroup.getSelectedToggle()).getUserData()
                .equals(GameStatus.PLAYER_DISCONNECTED.getText());
        int gameId = Integer.parseInt(((RadioButton) gameIdToggleGroup.getSelectedToggle()).getId());
        if(isReconnect){
            showReconnectGameDialog(gameId);
            return;
        }
        client.getController().sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    private void showReconnectGameDialog(int gameId){
        reconnectPopUp.setVisible(true);
        Animator.doFadeAnimation(reconnectPopUp,true);
        Animator.doPopAnimation(reconnectPopUp.getChildren().getFirst(),animationOffset,true);
        currentSelectedId = gameId;
    }

    /**
     * Shows the color-choosing buttons.
     *
     * @param colors available colors.
     * @param gameId the game's id.
     */
    public void showJoinGameDialog(List<Content> colors, int gameId){
        currentSelectedId = gameId;
        if(!createPopupGrid.isVisible() && !joinPopupGrid.isVisible()){
            Animator.doFadeAnimation(joinPopupGrid,true);
            Animator.doPopAnimation(joinPopupGrid.getChildren().getFirst(),animationOffset,true);
        }
        createPopupGrid.setVisible(false);
        colorChoiceToggleGroup = new ToggleGroup();
        colorChoiceGrid.getChildren().clear();
        while(!colorChoiceGrid.getColumnConstraints().isEmpty()){
            colorChoiceGrid.getColumnConstraints().removeFirst();
        }
        List<GridEntry> entries = new ArrayList<>();
        for(Content color : colors){
            RadioButton colorRadioButton = createRadioButton("", colorChoiceToggleGroup, "colorRadioButton");
            colorRadioButton.setStyle(String.format("-radio-color: %s;", color.getHexColorString()));
            colorRadioButton.setUserData(color.name());
            colorRadioButton.setAlignment(Pos.CENTER);
            colorRadioButton.setOnMouseClicked((mouseEvent) -> checkJoinInput());
            entries.add(new GridEntry(100, colorRadioButton));
            if(colorChoiceToggleGroup.getSelectedToggle() == null){
                colorChoiceToggleGroup.selectToggle(colorRadioButton);
            }
        }
        addColumns(colorChoiceGrid, entries);
        joinPopupGrid.setVisible(true);
    }

    /**
     * By firstly checking the join input, enables the "join" button.
     */
    @FXML
    public void checkJoinInput(){
        String nickname = nicknameTextBox.getText();
        joinPopupButton.setDisable(nickname.isEmpty() ||
                nickname.length() > GameParameters.getMaxNicknameLength() ||
                nickname.contains(GameParameters.getCommandChar()) ||
                nickname.contains(GameParameters.getDelimiter()) ||
                colorChoiceToggleGroup.getSelectedToggle() == null);
    }

    /**
     * Checks if the reconnection input is valid. If so, it disables the "reconnect" button.
     */
    @FXML
    public void checkReconnectInput(){
        String nickname = reconnectNicknameTextbox.getText();
        reconnectButton.setDisable(nickname.isEmpty() ||
                nickname.length() > GameParameters.getMaxNicknameLength());
    }

    /**
     * Tries to join to the previously specified match.
     */
    @FXML
    public void tryJoin(){
        joinPopupButton.setDisable(true);
        nicknameTextBox.setDisable(true);
        for(Toggle toggle : colorChoiceToggleGroup.getToggles()){
            if(toggle instanceof RadioButton radioButton){
                radioButton.setDisable(true);
            }
        }
        Content color = Content.valueOf(((RadioButton)colorChoiceToggleGroup.getSelectedToggle()).getUserData().toString());
        String nickname = nicknameTextBox.getText();
        client.getController().sendMessage(new JoinGameMessage(Status.JOIN_GAME, nickname, color, null, currentSelectedId));
    }

    /**
     * Tries a reconnection to the server.
     */
    @FXML
    public void tryReconnect(){
        reconnectButton.setDisable(true);
        reconnectNicknameTextbox.setDisable(true);
        String nickname = reconnectNicknameTextbox.getText();
        client.getController().sendMessage(new JoinGameMessage(Status.RECONNECT, nickname, null, null, currentSelectedId));
    }

    /**
     * Shows a custom critical error pop-up.
     * Once popped out, the user can return to the match selection by clicking on the "ok" button.
     *
     * @param message the error message contained in the pop-up.
     */
    public void showCriticalError(String message){
        errorLabel.setText(message);
        okButton.setUserData("critical");
        errorPopupGrid.setVisible(true);
    }

    /**
     * Shows a custom error pop-up.
     * Once popped out, the user can return to the game joining pop-up by clicking on the "ok" button.
     *
     * @param message the error message contained in the pop-up.
     */
    public void showUserError(String message){
        errorLabel.setText(message);
        okButton.setUserData("non-critical");
        errorPopupGrid.setVisible(true);
        Animator.doFadeAnimation(errorPopupGrid,true);
        Animator.doPopAnimation(errorPopupGrid.getChildren().getFirst(),animationOffset,true);
    }

    /**
     * Returns either to the match browsing page or the game joining pop-up, depending on the error nature.
     */
    @FXML
    public void handleOkButton(){
        if (okButton.getUserData().equals("critical")) {
            client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
        } else {
            client.getController().sendMessage(new IntegerMessage(Status.REQUEST_COLORS, currentSelectedId));
            nicknameTextBox.setDisable(false);
            joinPopupButton.setDisable(true);
            errorPopupGrid.setVisible(false);
        }
    }

    /**
     * Closes the join game popup and gets back to the available games browsing.
     */
    @FXML
    public void goBackFromPopup(){
        animateAndHidePopUp(joinPopupGrid);
        animateAndHidePopUp(createPopupGrid);
    }

    private void animateAndHidePopUp(GridPane createPopupGrid) {
        if(createPopupGrid.isVisible()){
            Animator.doFadeAnimation(createPopupGrid,false).setOnFinished((e) -> {
                client.getController().sendMessage(new Message(Status.REQUEST_GAMES));
                joinPopupGrid.setVisible(false);
                createPopupGrid.setVisible(false);
                reconnectPopUp.setVisible(false);
            });
            Animator.doPopAnimation(createPopupGrid.getChildren().getFirst(),animationOffset,false);
        }
    }
}