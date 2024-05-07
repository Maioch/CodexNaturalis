package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.TCPHandler;
import it.polimi.ingsw.network.client.ClientMessageHandler;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.messages.setup.NewGameMessage;
import it.polimi.ingsw.view.SetupView;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SetupCLI implements SetupView {
    private static final HashMap<Content, String> textColors = new HashMap<>(){{
        put(Content.RED, "\\u001B[31mRed\\u001B[0m");
        put(Content.BLUE, "\\u001B[34mBlue\\u001B[0m");
        put(Content.GREEN, "\\u001B[32mGreen\\u001B[0m");
        put(Content.PURPLE, "\\u001B[35mPurple\\u001B[0m");
    }};

    private final ClientMessageHandler messageHandler;

    public SetupCLI(){
        this.messageHandler = new ClientMessageHandler(this, new TerminalSubmitter());
        new Thread(messageHandler).start();
        System.out.print("Please enter the IP of the server you want to play on: ");
        String ip = getUserStringChoice(15, "IP address");
        System.out.print("Now enter the Port of the server: ");
        int port = getUserIntChoice(0, 65535);
        try (Socket socket = new Socket(ip, port)){
            socket.getInputStream();
            TCPHandler tcpHandler = new TCPHandler(socket, messageHandler);
            messageHandler.setNetworkHandler(tcpHandler);
            Thread listeningThread = new Thread(tcpHandler);
            listeningThread.start();
            messageHandler.sendMessage(new Message(Status.REQUEST_GAMES));
            try {
                listeningThread.join();
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }catch (IOException e){
            System.out.println("pippa");
            System.out.println(e.getMessage());
        }
    }

    public void updateMatchList(HashMap<Integer,String> matchList){
        System.out.println("Here's the match list:");
        System.out.println();
        for (Map.Entry<Integer, String> entry : matchList.entrySet()){
            System.out.printf("Match %7d: %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        System.out.println("Enter the ID of the match you want to join (0 for a new match)");
        int id = -1;
        while (!matchList.containsKey(id) || id == 0) {
            id = getUserIntChoice(
                    0,
                    matchList.keySet().stream().max(Integer::compareTo).orElse(0));
            if (id == 0) {
                System.out.print("Enter the new match's name: ");
                String gameName = getUserStringChoice(GameParameters.getMaxNicknameLength(), "match name");
                int minPlayers = GameParameters.getMinPlayers();
                int maxPlayers = GameParameters.getMaxPlayers();
                System.out.printf("Now enter the number of players (between %d and %d inclusive): ", minPlayers, maxPlayers);
                int numberOfPlayers = getUserIntChoice(minPlayers, maxPlayers);
                messageHandler.sendMessage(new NewGameMessage(gameName, numberOfPlayers));
            } else {
                messageHandler.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, id));
            }
        }
    }

    public void newGameSuccess(int gameId){
        System.out.println("You have created a new match, whose ID is: " + gameId);
        messageHandler.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    public void showCriticalError(String message){
        System.out.println(message);
        messageHandler.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    public void showJoinGameDialog(ArrayList<Content> colors, int gameId){
        System.out.println("You are trying to join a match...");
        System.out.println("First, choose a color from the list below by entering the corresponding index: ");
        for(int i = 0; i < colors.size(); i++){
            System.out.print((i + 1) + ". " + textColors.get(colors.get(i)));
            System.out.println(colors.get(i) != colors.getLast() ? ", " : ".");
        }
        int colorIndex = getUserIntChoice(1, colors.size()) - 1;
        System.out.print("Now enter your nickname: ");
        String nickname = getUserStringChoice(GameParameters.getMaxNicknameLength(), "nickname");
        messageHandler.sendMessage(new JoinGameMessage(nickname, colors.get(colorIndex), gameId));
    }

    public void showUserError(String message, int gameId){
        System.out.println(message);
        messageHandler.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    public void showSuccessfulJoin(){
        System.out.println("You have successfully joined the game!");
    }

    private int getUserIntChoice(int min, int max){
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

    private String getUserStringChoice(int maxLength, String subject){
        Scanner userInput = new Scanner(System.in);
        String userChoice;
        while(true){
            userChoice = userInput.nextLine();
            if(userChoice.length() <= maxLength){
                return userChoice;
            }
            System.out.println("The " + subject + " can't be longer than " + maxLength + " characters!");
        }
    }
}