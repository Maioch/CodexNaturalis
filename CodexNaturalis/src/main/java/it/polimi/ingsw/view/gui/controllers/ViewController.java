package it.polimi.ingsw.view.gui.controllers;

import it.polimi.ingsw.network.client.ClientController;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.text.TextAlignment;

public abstract class ViewController {
    protected ClientController controller;

    /**
     * Sets the client controller.
     * @param controller the client controller.
     */
    public void setController(ClientController controller) {
        this.controller = controller;
    }

    /**
     * Creates a radio button.
     * @param buttonText the contained in button text.
     * @param group the button's group.
     * @param styleClass the css style class
     * @return the created radio button.
     */
    protected RadioButton createRadioButton(String buttonText, ToggleGroup group, String styleClass){
        RadioButton radioButton = createRadioButton(buttonText, group);
        radioButton.getStyleClass().add(styleClass);
        return radioButton;
    }

    /**
     * Creates a radio button.
     *
     * @param buttonText the contained in button text.
     * @param group      the button's group.
     * @return           the created radio button.
     */
    protected RadioButton createRadioButton(String buttonText, ToggleGroup group){
        RadioButton radioButton = new RadioButton(buttonText);
        radioButton.setToggleGroup(group);
        radioButton.setMaxWidth(Double.MAX_VALUE);
        radioButton.setTextAlignment(TextAlignment.CENTER);
        return radioButton;
    }
}