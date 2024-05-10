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

public class SetupCLI implements SetupView {
    private static final Map<Content, String> textColors = new HashMap<>(){{
        put(Content.RED, "\\u001B[31mRed\\u001B[0m");
        put(Content.BLUE, "\\u001B[34mBlue\\u001B[0m");
        put(Content.GREEN, "\\u001B[32mGreen\\u001B[0m");
        put(Content.PURPLE, "\\u001B[35mPurple\\u001B[0m");
    }};

    private final ClientController clientController;

    public SetupCLI(){
        //TODO:handle connection failures more gracefully
        this.clientController = new ClientController(this, new TerminalSubmitter());
        new Thread(clientController).start();
        System.out.print("Please enter the IP of the server you want to play on: ");
        String ip = UtilitiesCLI.getUserStringChoice(15, "IP address");
        System.out.print("Now enter the Port of the server: ");
        int port = UtilitiesCLI.getUserIntChoice(0, 65535);
        System.out.println("Which protocol would you like to use? Enter 1 for TCP or 2 for RMI.");
        int protocol = UtilitiesCLI.getUserIntChoice(1,2);
        switch(protocol){
            case 1 -> {
                try {
                    Socket socket = new Socket(ip, port);
                    socket.getInputStream();
                    TCPHandler tcpHandler = new TCPHandler(socket, clientController);
                    clientController.setNetworkHandler(tcpHandler);
                    new Thread(tcpHandler).start();
                    clientController.sendMessage(new Message(Status.REQUEST_GAMES));
                }catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
            case 2 -> {
                try {
                    RMISetup rmiSetup = (RMISetup) Naming.lookup(String.format("//%s:%d/RMIManager",ip,port));
                    try{
                        RMIHandler rmiHandler = new RMIHandler(clientController);
                        clientController.setNetworkHandler(rmiHandler);
                        rmiSetup.register(rmiHandler);
                        clientController.sendMessage(new Message(Status.REQUEST_GAMES));
                    }catch (IOException e){
                        System.out.println(e.getMessage());
                    }
                }catch (MalformedURLException e) {
                    throw new RuntimeException("The RMI URL is malformed");
                } catch (NotBoundException e) {
                    throw new RuntimeException("The requested object isn't bound");
                } catch (RemoteException e) {
                    throw new RuntimeException(e.getMessage());
                }
            }

        }
    }

    public void updateMatchList(Map<Integer,String> matchList){
        System.out.println("Here's the match list:");
        System.out.println();
        for (Map.Entry<Integer, String> entry : matchList.entrySet()){
            System.out.printf("Match %7d: %s\n", entry.getKey(), entry.getValue());
        }
        System.out.println();
        System.out.println("Enter the ID of the match you want to join (0 for a new match)");
        int id = -1;
        while (!matchList.containsKey(id)) {
            id = UtilitiesCLI.getUserIntChoice(
                    0,
                    matchList.keySet().stream().max(Integer::compareTo).orElse(0));
            if (id == 0) {
                System.out.print("Enter the new match's name: ");
                String gameName = UtilitiesCLI.getUserStringChoice(GameParameters.getMaxNicknameLength(), "match name");
                int minPlayers = GameParameters.getMinPlayers();
                int maxPlayers = GameParameters.getMaxPlayers();
                System.out.printf("Now enter the number of players (between %d and %d inclusive): ", minPlayers, maxPlayers);
                int numberOfPlayers = UtilitiesCLI.getUserIntChoice(minPlayers, maxPlayers);
                clientController.sendMessage(new NewGameMessage(gameName, numberOfPlayers));
                break;
            }
            clientController.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, id));
        }
    }

    public void newGameSuccess(int gameId){
        System.out.println("You have created a new match, whose ID is: " + gameId);
        clientController.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    public void showCriticalError(String message){
        System.out.println(message);
        clientController.sendMessage(new Message(Status.REQUEST_GAMES));
    }

    public void showJoinGameDialog(List<Content> colors, int gameId){
        System.out.println("You are trying to join a match...");
        System.out.println("First, choose a color from the list below by entering the corresponding index: ");
        for(int i = 0; i < colors.size(); i++){
            System.out.print((i + 1) + ". " + textColors.get(colors.get(i)));
            System.out.println(colors.get(i) != colors.getLast() ? ", " : ".");
        }
        int colorIndex = UtilitiesCLI.getUserIntChoice(1, colors.size()) - 1;
        System.out.print("Now enter your nickname: ");
        String nickname = UtilitiesCLI.getUserStringChoice(GameParameters.getMaxNicknameLength(), "nickname");
        clientController.sendMessage(new JoinGameMessage(nickname, colors.get(colorIndex), gameId));
    }

    public void showUserError(String message, int gameId){
        System.out.println(message);
        clientController.sendMessage(new IntegerMessage(Status.REQUEST_COLORS, gameId));
    }

    public void showSuccessfulJoin(){
        System.out.println("You have successfully joined the game!");
        GameCLI gameCLI = new GameCLI(clientController);
        clientController.setGameView(gameCLI);
    }
}