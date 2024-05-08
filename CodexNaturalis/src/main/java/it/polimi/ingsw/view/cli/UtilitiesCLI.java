package it.polimi.ingsw.view.cli;

import java.util.Scanner;

public class UtilitiesCLI {
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

    public static String getUserStringChoice(){
        return new Scanner(System.in).nextLine();
    }

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
}