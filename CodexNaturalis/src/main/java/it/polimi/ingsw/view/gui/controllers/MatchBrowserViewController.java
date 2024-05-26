package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.controller.GameStatus;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;

import java.util.*;

public class MatchBrowserViewController {
    private final List<List<RadioButton>> radioButtons = new ArrayList<>();
    private ClientController controller;

    @FXML
    public Button createButton;

    @FXML
    public GridPane matchGridPane;

    @FXML
    public GridPane errorPopup;

    @FXML
    public Label errorText;

    @FXML
    public Button okButton;

    @FXML
    public GridPane joinGamePopup;

    @FXML
    public TextField nicknameTextBox;

    @FXML
    public Button joinPopupButton;

    @FXML
    public Button refreshButton;

    @FXML
    public Button joinButton;

    private ToggleGroup groupId;

    public void setController (ClientController controller) { this.controller = controller; }

    /**
     * Method that adds all matches to the match list.
     * @param matchList the list of matches.
     */
    public void addMatches(List<GameInfo> matchList){
        groupId = new ToggleGroup();
        ToggleGroup groupName = new ToggleGroup();
        ToggleGroup groupStatus = new ToggleGroup();
        for(GameInfo match : matchList) {
            int id = match.gameId();
            GridPane grid = new GridPane();
            RadioButton idButton = createRadioButton(String.valueOf(id), match.gameStatus(), groupId);
            idButton.setId(String.valueOf(id));
            RadioButton nameButton = createRadioButton(match.gameName(), match.gameStatus(), groupName);
            RadioButton statusButton = createRadioButton(match.gameStatus().getText(), match.gameStatus(), groupStatus);
            radioButtons.add(Arrays.asList(idButton,nameButton,statusButton));
            grid.addRow(0,idButton,nameButton,statusButton);
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
     * Creates a radio button.
     * @param buttonText the contained in button text.
     * @param gameStatus the current game status.
     * @param group the button's group.
     * @return the created radio button.
     */
    private RadioButton createRadioButton(String buttonText, GameStatus gameStatus, ToggleGroup group){
        RadioButton button = new RadioButton(buttonText);
        button.setToggleGroup(group);
        button.setMaxWidth(Double.MAX_VALUE);
        setupButton(button);
        button.setDisable(gameStatus == GameStatus.STARTED);
        return button;
    }

    /**
     * Method used to add CSS effects and method callbacks to the matches list entries.
     * @param radioButton the entry to stylize.
     */
    private void setupButton(RadioButton radioButton){
        radioButton.setTextAlignment(TextAlignment.CENTER);
        radioButton.getStyleClass().clear();
        radioButton.getStyleClass().add("tableButton");
        radioButton.setOnMouseClicked((mouseEvent) -> {
            joinButton.setDisable(false);
            disableButtonRow(mouseEvent);
            setRadioButtonStyle(mouseEvent, "tableButton");
        });
        radioButton.setOnMousePressed(mouseEvent -> setRadioButtonStyle(mouseEvent, "pressedTableButton"));
        radioButton.setOnMouseEntered(mouseEvent -> setRadioButtonStyle(mouseEvent, "hoveredTableButton"));
        radioButton.setOnMouseExited(mouseEvent -> setRadioButtonStyle(mouseEvent, "tableButton"));
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
    public void createMatch(){

    }

    @FXML
    public void joinMatch(){
        controller.sendMessage(new IntegerMessage(Status.REQUEST_COLORS,
                Integer.parseInt(((RadioButton) groupId.getSelectedToggle()).getId())));
    }

    /**
     * Refreshes the match list (by requesting the games list).
     */
    @FXML
    public void refreshMatchList(){
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
    }
}