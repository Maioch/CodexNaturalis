package it.polimi.ingsw.view.cli;

import com.fasterxml.jackson.databind.JsonNode;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardBuilder;

import java.util.Scanner;

/**
 * A class that embeds some useful static methods (for cli-developing contexts)
 */
public class UtilitiesCLI {
    /**
     * A method that gets (from command-line) an integer input, checking if it's valid
     * @param min the minimum accepted value
     * @param max the maximum accepted value
     * @return the integer input
     */
    public static int getUserIntChoice(int min, int max){
        Scanner userInput = new Scanner(System.in);
        int userChoice;
        while(true){
            if(!userInput.hasNextInt()){
                userInput.next();
                System.out.println("Please, write an integer value among those you see above: ");
                continue;
            }
            userChoice = userInput.nextInt();
            if(userChoice >= min && userChoice <= max) {
                return userChoice;
            }
            System.out.println("The choice you have made isn't correct, please try again!");
        }
    }

    /**
     * A method that gets (from command-line) a string input
     * @return the read input
     */
    public static String getUserStringChoice(){
        return new Scanner(System.in).nextLine();
    }

    /**
     * A method that gets (from command-line) string input, checking if it's valid
     * @param maxLength the maximum accepted string length
     * @param subject the subject of choice
     * @return the stream choice (input)
     */
    public static String getUserStringChoice(int maxLength, String subject){
        String userChoice;
        while(true){
            userChoice = getUserStringChoice();
            if(userChoice.length() <= maxLength){
                return userChoice;
            }
            System.out.println("The " + subject + " can't be longer than " + maxLength + " characters!");
        }
    }

    /**
     * A method that gets a certain card bonus infos
     * @param card the card to get the infos from
     * @param cardType the card's type (PLACEABLE or OBJECTIVE)
     * @return a formatted string of the bonus infos
     */
    public static String getBonusInfo(BasicCard card, String cardType){
        JsonNode bonusNode = CardBuilder.getCardJson(card.getCardId(), cardType).get("bonus");
        if(bonusNode == null)
            return "no bonus.";
        return switch (bonusNode.get("type").asText()){
            case "CORNER" -> "corner.";
            case "OBJECT" -> "object -> " + bonusNode.get("object").asText();
            case "CONTENT" -> "a";
            case "PATTERN" -> "b";
            default -> "no bonus.";
        };
    }

    /**
     * A method that gets a certain card native points
     * @param card the card to get the infos from
     * @param cardType the card's type (PLACEABLE or OBJECTIVE)
     * @return the native points
     */
    public static int getNativePoints(BasicCard card, String cardType){
        return CardBuilder.getPoints(CardBuilder.getCardJson(card.getCardId(), cardType));
    }
}