package it.polimi.ingsw.view.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;

import java.util.*;

public class MatchBrowserViewController {
    private final List<Pair<RadioButton, RadioButton>> radioButtons = new ArrayList<>();

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
    public TextField joinPopupButton;

    @FXML
    public Button refreshButton;

    @FXML
    public Button joinButton;

    /**
     * Method that adds all matches to the match list.
     * @param matchList the list of matches.
     */
    public void addMatches(Map<Integer, String> matchList){
        ToggleGroup groupId = new ToggleGroup();
        ToggleGroup groupName = new ToggleGroup();
        for(Map.Entry<Integer, String> match : matchList.entrySet()) {
            int id = match.getKey();
            String name = match.getValue();
            GridPane grid = new GridPane();
            RadioButton idButton = new RadioButton(String.valueOf(id));
            idButton.setToggleGroup(groupId);
            idButton.setId(String.valueOf(id));
            buttonStyler(idButton);
            RadioButton nameButton = new RadioButton(name);
            nameButton.setId(String.valueOf(id));
            nameButton.setToggleGroup(groupName);
            buttonStyler(nameButton);
            radioButtons.add(new Pair<>(idButton, nameButton));
            grid.addColumn(0, idButton);
            grid.addColumn(1, nameButton);
            matchGridPane.addRow(matchGridPane.getRowCount(), grid);
        }
    }

    /**
     * Method used to add CSS effects to the matches list entries.
     * @param button the entry to stylize.
     */
    private void buttonStyler(RadioButton button){
        button.setTextAlignment(TextAlignment.CENTER);
        button.styleProperty().setValue("tableButton");
        button.setOnMouseReleased(this::disableButtonRow);
    }

    /**
     * Method that disables the newly selected row
     * @param mouseEvent the event that causes the method run
     */
    @FXML
    public void disableButtonRow(MouseEvent mouseEvent){
        RadioButton sender = (RadioButton) mouseEvent.getSource();
        Pair<RadioButton, RadioButton> currentPair = radioButtons.stream()
                .filter(pair -> (pair.getKey().getId().equals(sender.getId()))).findFirst().orElseThrow();
        if (currentPair.getKey().equals(sender)) {
            currentPair.getValue().setSelected(true);
        } else {
            currentPair.getKey().setSelected(true);
        }
    }
}