package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.RMIHandler;
import it.polimi.ingsw.network.TCPHandler;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.messages.setup.NewGameMessage;
import it.polimi.ingsw.network.server.RMISetup;
import it.polimi.ingsw.view.SetupView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Class used when the client chooses to play the TUI version of the game; this class represents the CLI for the player
 * reception phase, before entering an actual game.
 */
public class SetupCLI extends AbstractCLI implements SetupView {

    private final ClientController clientController;
    private boolean isConnected;

    /**
     * Constructor for the class. The method asks the player the ip address and the port of the server, then it makes
     * him choose if he wants to connect using socket or RMI protocol: the method then handles the connection.
     */
    public SetupCLI(){
        isConnected = false;
        this.clientController = new ClientController(this, new TerminalSubmitter());
        while (!isConnected) {
            new Thread(clientController).start();
            String ip = readFromInput("Please enter the IP of the server you want to play on: ",
                    (s -> s.length() <= 15),
                    this::stringIdentity);
            int port = readFromInput("Now enter the Port of the server: ",
                    (s -> s >= 0 && s <= 65535),
                    this::stringToInt);
            int protocol = readFromInput("Which protocol would you like to use? Enter 1 for TCP or 2 for RMI.",
                    (s -> s >= 1 && s <= 2),
                    this::stringToInt);
            switch (protocol) {
                case 1 -> {
                    try {
                        Socket socket = new Socket(ip, port);
                        socket.getInputStream();
                        TCPHandler tcpHandler = new TCPHandler(socket, clientController);
                        clientController.setNetworkHandler(tcpHandler);
                        new Thread(tcpHandler).start();
                        clientController.sendMessage(new Message(Status.REQUEST_GAMES));
                        isConnected = true;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        RMISetup rmiSetup = (RMISetup) Naming.lookup(String.format("//%s:%d/RMIManager", ip, port));
                        try {
                            RMIHandler rmiHandler = new RMIHandler(clientController);
                            clientController.setNetworkHandler(rmiHandler);
                            rmiSetup.register(rmiHandler);
                            clientController.sendMessage(new Message(Status.REQUEST_GAMES));
                            isConnected = true;
                        } catch (IOException e) {
                            System.out.println(e.getMessage());
                        }
                    } catch (MalformedURLException e) {
                        System.out.println("No RMI Server was found at the supplied address");
                    } catch (NotBoundException e) {
                        System.out.println("The requested object isn't bound");
                    } catch (RemoteException e) {
                        System.out.println("Couldn't connect to the RMI server");
                    }
                }
            }
        }
    }

    /**
     * This method handles the first step of the player's reception, printing a list of all the available matches
     * along with the index; it then asks the index of the game the player wants to join: if the player enters "0", the
     * method also handles the creation of a new game.
     * @param matchList the list of all available matches.
     */
    @Override
    public void updateMatchList(Map<Integer,String> matchList){
        System.out.println("Here's the match list:");
        System.out.println();
        for (Map.Entry<Integer, String> entry : matchList.entrySet()){
            System.out.printf("Match %7d: %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        int id = readFromInput("Enter the ID of the match you want to join (0 for a new match)",
                (i -> !matchList.containsKey(i) || i == 0),
                this::stringToInt);
        Message messageToSend = new IntegerMessage(Status.REQUEST_COLORS, id);
        if (id == 0) {
            System.out.print("Enter the new match's name: ");
            String gameName = readFromInput("Enter the new match's name: ",
                    (s -> s.length() <= GameParameters.getMaxNicknameLength()),
                    this::stringIdentity);
            int minPlayers = GameParameters.getMinPlayers();
            int maxPlayers = GameParameters.getMaxPlayers();
            int numberOfPlayers = readFromInput(
                    String.format("Now enter the number of players (between %d and %d inclusive): ", minPlayers, maxPlayers),
                    (n -> n < GameParameters.getMaxPlayers() && n > GameParameters.getMinPlayers()),
                    this::stringToInt);
            messageToSend = new NewGameMessage(gameName, numberOfPlayers);
        }
        clientController.sendMessage(messageToSend);
    }

    /**
     * Method used to inform a player that requested a new game creation that the process was successful.
     * @param gameId the ID of the new game created, printed by the method.
     */
    @Override
    public void newGameSuccess(int gameId){
        System.out.println("You have created a new match, whose ID is: " + gameId);
        clientController.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Method that prints a custom critical error message and returns the client to the match selection.
     * @param message the message to print.
     */
    @Override
    public void showCriticalError(String message){
        System.out.println(message);
        clientController.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * This method handles the second step of the player's reception: it makes the player choose a color from
     * those available in the game he's trying to join, then it asks the player to choose his nickname.
     * This method is used also with a player that just created a new game.
     * @param colors the list of a match's available colors.
     * @param gameId the id of the game the player is trying to join.
     */
    @Override
    public void showJoinGameDialog(List<Content> colors, int gameId){
        System.out.println("You are trying to join a match");
        for(int i = 0; i < colors.size(); i++){
            System.out.print((i + 1) + ". " + textColors.get(colors.get(i)));
            System.out.println(colors.get(i) != colors.getLast() ? ", " : ".");
        }
        int colorIndex = readFromInput("First, choose a color from the list above by entering the corresponding index: ",
                (i -> i > 1 && i < colors.size()),
                this::stringToInt) - 1;
        String nickname = readFromInput("Now enter your nickname: ",
                (s -> !s.isBlank() && s.length() < GameParameters.getMaxNicknameLength()),
                this::stringIdentity);
        clientController.sendMessage(new JoinGameMessage(nickname, colors.get(colorIndex), gameId));
    }

    /**
     * Method that prints a custom error message and returns the client to the color selection.
     * This method is usually called when the player chooses an invalid color or nickname.
     * @param message the message printed by the method.
     * @param gameId the id of the game the player is trying to join.
     */
    @Override
    public void showUserError(String message, int gameId){
        System.out.println(message);
        clientController.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Method that notifies the player that he successfully joined his desired game.
     */
    @Override
    public void showSuccessfulJoin(){
        System.out.println("You have successfully joined the game!");
        GameCLI gameCLI = new GameCLI(clientController);
        clientController.setGameView(gameCLI);
    }

    /**
     * Method used to handle the "REFRESH" command that may be inputted by the player; this command refreshes the available
     * games list given to the player.
     * @param command the command inputted by the client.
     * @param argument the arguments of the player inputted command.
     */
    @Override
    protected void checkCommand(String command, String argument){
        if(!isConnected){
            return;
        }
        //TODO:FIX REFRESH BUG
        switch (command.toUpperCase()){
            case "REFRESH" -> clientController.sendMessage(new Message(Status.REQUEST_GAMES));
            default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
        }
    }
}