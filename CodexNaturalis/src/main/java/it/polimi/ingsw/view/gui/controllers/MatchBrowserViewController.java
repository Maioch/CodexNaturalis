package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.controller.GameStatus;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * Class used to handle the march browsing scene for the GUI.
 */
public class MatchBrowserViewController {
    @FXML
    public Button createButton;
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
    private final List<List<RadioButton>> radioButtons = new ArrayList<>();
    private ClientController controller;
    private ToggleGroup gameIdToggleGroup;
    private ToggleGroup colorChoiceToggleGroup;
    private int currentSelectedId;

    public void setController (ClientController controller) {
        this.controller = controller;
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
            RadioButton nameRadioButton = createRadioButton(match.getGameName(), groupName, "tableButton");
            setupMatchListButton(nameRadioButton);
            nameRadioButton.setDisable(match.getGameStatus() == GameStatus.STARTED);
            RadioButton statusRadioButton = createRadioButton(match.getGameStatus().getText(), groupStatus, "tableButton");
            setupMatchListButton(statusRadioButton);
            statusRadioButton.setDisable(match.getGameStatus() == GameStatus.STARTED);
            radioButtons.add(Arrays.asList(idRadioButton,nameRadioButton,statusRadioButton));
            grid.addRow(0,idRadioButton,nameRadioButton,statusRadioButton);
            grid.getColumnCount();
            ColumnConstraints idColumnConstraint = new ColumnConstraints();
            idColumnConstraint.setPercentWidth(12);
            ColumnConstraints nameColumnConstraint = new ColumnConstraints();
            nameColumnConstraint.setPercentWidth(53);
            ColumnConstraints statusColumnConstraint = new ColumnConstraints();
            statusColumnConstraint.setPercentWidth(35);
            grid.getColumnConstraints().addAll(idColumnConstraint, nameColumnConstraint, statusColumnConstraint);
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
     * Creates a radio button.
     * @param buttonText the contained in button text.
     * @param group the button's group.
     * @param styleClass the css style class
     * @return the created radio button.
     */
    private RadioButton createRadioButton(String buttonText, ToggleGroup group, String styleClass){
        RadioButton radioButton = createRadioButton(buttonText, group);
        radioButton.getStyleClass().add(styleClass);
        return radioButton;
    }

    /**
     * Creates a radio button.
     *
     * @param buttonText the contained in button text.
     * @param group      the button's group.
     *
     * @return           the created radio button.
     */
    private RadioButton createRadioButton(String buttonText, ToggleGroup group){
        RadioButton radioButton = new RadioButton(buttonText);
        radioButton.setToggleGroup(group);
        radioButton.setMaxWidth(Double.MAX_VALUE);
        radioButton.setTextAlignment(TextAlignment.CENTER);
        return radioButton;
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

    @FXML
    public void requestCreation(){
        
    }

    /**
     * Starts the game joining procedure (by sending a "REQUEST_COLORS" message).
     */
    @FXML
    public void requestJoin(){
        controller.sendMessage(new IntegerMessage(Status.REQUEST_COLORS,
                Integer.parseInt(((RadioButton) gameIdToggleGroup.getSelectedToggle()).getId())));
    }

    /**
     * Shows the color-choosing buttons.
     *
     * @param colors available colors.
     * @param gameId the game's id.
     */
    public void showJoinGameDialog(List<Content> colors, int gameId){
        currentSelectedId = gameId;
        colorChoiceToggleGroup = new ToggleGroup();
        int i = 0;
        for(Content color : colors){
            RadioButton colorRadioButton = createRadioButton("", colorChoiceToggleGroup, "colorRadioButton");
            colorRadioButton.setStyle(String.format("-radio-color: %s;", color.getHexColorString()));
            colorRadioButton.setAlignment(Pos.CENTER);
            colorRadioButton.setOnMouseClicked((mouseEvent) -> checkInput());
            ColumnConstraints columnConstraint = new ColumnConstraints();
            columnConstraint.setPercentWidth((double) 100 / colors.size());
            colorChoiceGrid.getColumnConstraints().add(columnConstraint);
            colorChoiceGrid.addColumn(i, colorRadioButton);
            i++;
        }
        joinPopupGrid.setVisible(true);
    }

    //@FXML
    public void checkInput(){
        String nickname = nicknameTextBox.getText();
        joinPopupButton.setDisable(nickname.isEmpty() ||
                nickname.length() > GameParameters.getMaxNicknameLength() ||
                colorChoiceToggleGroup.getSelectedToggle() == null);
    }

    /**
     * Refreshes the match list (by requesting the games list).
     */
    @FXML
    public void refreshMatchList(){
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    @FXML
    public void tryJoin(){
        Content color = Content.valueOf(((RadioButton)colorChoiceToggleGroup.getSelectedToggle()).getText().toUpperCase());
        String nickname = nicknameTextBox.getText();
        controller.sendMessage(new JoinGameMessage(Status.JOIN_GAME, nickname, color, currentSelectedId));
    }
}