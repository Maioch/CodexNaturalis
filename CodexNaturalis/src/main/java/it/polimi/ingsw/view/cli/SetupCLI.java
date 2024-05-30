package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.controller.GameInfo;
import it.polimi.ingsw.controller.GameStatus;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.messages.setup.NewGameMessage;
import it.polimi.ingsw.view.SetupView;

import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Class used when the client chooses to play the TUI version of the game; this class represents the CLI for the player
 * reception phase, before entering an actual game.
 */
public class SetupCLI extends AbstractCLI implements SetupView {
    private final ClientController controller;

    /**
     * Constructor for the class. The method asks the player the ip address and the port of the server, then it makes
     * them choose if they want to connect using socket or RMI protocol: the method then handles the connection.
     */
    public SetupCLI(){
        CLIActionHandler cliActionHandler = new CLIActionHandler();
        boolean isConnected = false;
        this.controller = new ClientController(this, new TerminalSubmitter(cliActionHandler));
        new Thread(controller).start();
        new Thread(cliActionHandler).start();
        while (!isConnected) {
            String ip = readFromInput("Please enter the IP of the server you want to play on: ",
                    (s -> s.length() <= 15 && Pattern.compile("[0-9]{0,3}\\.[0-9]{0,3}\\.[0-9]{0,3}\\.[0.9]{0,3}").matcher(s).find()),
                    this::stringIdentity);
            int port = readFromInput("Now enter the Port of the server: ",
                    (s -> s >= 0 && s <= 65535),
                    this::stringToInt);
            int protocol = readFromInput("\nChoose the connection technology to use. Enter 1 for TCP or 2 for RMI: ",
                    (s -> s >= 1 && s <= 2),
                    this::stringToInt);
            switch (protocol) {
                case 1 -> {
                    try {
                        ConnectionInitializer.initializeTCP(ip, port, controller, new TerminalSubmitter(cliActionHandler));
                        System.out.println(GameParameters.getTitle());
                        isConnected = true;
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                case 2 -> {
                    try {
                        ConnectionInitializer.initializeRMI(ip, port, controller, new TerminalSubmitter(cliActionHandler));
                        System.out.println(GameParameters.getTitle());
                        isConnected = true;
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
    public void updateMatchList(List<GameInfo> matchList){
        System.out.println("Here are the available matches and their IDs:");
        System.out.println();
        int gameNameLength = - GameParameters.getMaxNicknameLength();
        for (GameInfo gameInfo: matchList){
            System.out.printf("%5d: %" + gameNameLength + "s %s\n",
                    gameInfo.getGameId(), gameInfo.getGameName(), gameInfo.getGameStatus().getText());
        }
        System.out.println();
        System.out.println("You can create a new game by entering 0 or refresh the list with -1");
        int id = readFromInput("To join an existing game enter the corresponding ID instead: ",
                (i -> matchList.stream().filter(g -> g.getGameStatus() != GameStatus.STARTED)
                        .map(GameInfo::getGameId).toList().contains(i) || i == 0 || i == -1),
                this::stringToInt);
        Message messageToSend;
        switch(id){
            case -1 -> messageToSend = new Message(Status.REQUEST_GAMES);
            case 0 -> {
                System.out.println("\nYou're creating a new game: please enter the requested information");
                String gameName = readFromInput("   Name: ",
                        (s -> s.length() <= GameParameters.getMaxNicknameLength()),
                        this::stringIdentity);
                int minPlayers = GameParameters.getMinPlayers();
                int maxPlayers = GameParameters.getMaxPlayers();
                int numberOfPlayers = readFromInput(
                        String.format("   Number of players (at least %d and not more than %d): ", minPlayers, maxPlayers),
                        (n -> n <= GameParameters.getMaxPlayers() && n >= GameParameters.getMinPlayers()),
                        this::stringToInt);
                messageToSend = new NewGameMessage(gameName, numberOfPlayers);
            }
            default -> {
                if(matchList.stream().filter(g -> g.getGameId() == id).findFirst().orElseThrow().getGameStatus() == GameStatus.LOBBY){
                    messageToSend = new IntegerMessage(Status.REQUEST_COLORS, id);
                } else {
                    System.out.println("\nWelcome back!");
                    String nickname = readFromInput("Please enter the name you chose when you first joined the game: ",
                            (s -> !s.isBlank() && s.length() < GameParameters.getMaxNicknameLength() && !s.contains(" ")
                                    && !s.contains(GameParameters.getCommandChar()) && !s.contains(GameParameters.getDelimiter())),
                            this::stringIdentity);
                    messageToSend = new JoinGameMessage(Status.RECONNECT, nickname, null, id);
                }
            }
        }
        controller.sendMessage(messageToSend);
    }

    /**
     * Method used to inform a player that requested a new game creation that the process was successful.
     * @param gameId the ID of the new game created, printed by the method.
     */
    @Override
    public void newGameSuccess(int gameId){
        System.out.println("You've successfully created a new match! Its ID is: " + gameId);
        controller.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Method that prints a custom critical error message and returns the client to the match selection.
     * @param message the message to print.
     */
    @Override
    public void showCriticalError(String message){
        System.out.println(message);
        controller.sendMessage(new Message(Status.REQUEST_GAMES));
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
        System.out.println("\nYou are trying to join the match.");
        System.out.println("Here are the available colors and their IDs: ");
        for(int i = 0; i < colors.size(); i++){
            System.out.printf("   %d. %s%s%s", (i + 1), colors.get(i).getTextColorString(),
                    colors.get(i).toString().toLowerCase(), Content.EMPTY.getTextColorString());
            System.out.println(colors.get(i) != colors.getLast() ? ", " : ".");
        }
        int colorIndex = readFromInput("Enter the ID of your chosen color: ",
                (i -> i >= 1 && i <= colors.size()),
                this::stringToInt) - 1;
        String nickname = readFromInput(String.format("Choose your nickname, without including '%s','%s', or spaces: ",
                        GameParameters.getCommandChar(), GameParameters.getDelimiter()),
                (s -> !s.isBlank() && s.length() < GameParameters.getMaxNicknameLength() && !s.contains(" ")
                        && !s.contains(GameParameters.getCommandChar()) && !s.contains(GameParameters.getDelimiter())),
                this::stringIdentity);
        controller.sendMessage(new JoinGameMessage(Status.JOIN_GAME, nickname, colors.get(colorIndex), gameId));
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
        controller.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    /**
     * Method that notifies the player that he successfully joined his desired game.
     */
    @Override
    public void showSuccessfulJoin(String nickname, Content color, int numberOfPlayers){
        System.out.println("\nYou have successfully joined the game!");
        System.out.printf("This game requires %d players to start\n", numberOfPlayers);
        GameCLI gameCLI = new GameCLI(controller);
        controller.setGameView(gameCLI);
    }

    /**
     * Method used to handle the "REFRESH" command that may be inputted by the player; this command refreshes the available
     * games list given to the player.
     * @param command the command inputted by the client.
     * @param argument the arguments of the player inputted command.
     */
    @Override
    protected void checkCommand(String command, String argument){
        switch (command.toUpperCase()){
            case "GETRULES" -> {
                try {
                    URI url = new URI(GameParameters.getRulesURL());
                    Desktop.getDesktop().browse(url);
                }catch(URISyntaxException | IOException | UnsupportedOperationException e){
                    System.out.printf(
                            "Couldn't launch the browser. Please open it yourself and navigate to %s \n", GameParameters.getRulesURL());
                }
            }
            case "ABOUT" ->  System.out.println(
                    "Original game by Cranio Creations. Developed by Fidanza, Gatti, Nisoli, and Maiocchi.");
            case "HELP" -> System.out.println(GameParameters.getSetupHelpBody());
            default -> System.out.println("Command not recognized, type /HELP for a list of all commands!");
        }
    }
}