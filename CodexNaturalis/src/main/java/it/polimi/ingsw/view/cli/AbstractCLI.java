package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.exceptions.MapperException;
import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.network.client.ClientController;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.client.ConnectionSettings;
import it.polimi.ingsw.network.messages.Message;
import it.polimi.ingsw.network.messages.Status;
import it.polimi.ingsw.view.ReconnectableView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.*;
import java.util.function.Predicate;

/**
 * Abstract class that contains all the methods used to handle the client's inputs.
 */
public abstract class AbstractCLI {
    protected ClientController controller;
    protected TerminalSubmitter terminalSubmitter;
    protected ConnectionSettings connectionSettings;

    /**
     * Method used to read the client's inputs.
     * @param prompt the message printed to ask the client to input something.
     * @param checker predicate that checks if the client's input is valid.
     * @param mapper mapper that converts the client's input into the correct requested type.
     * @return the converted client's input.
     * @param <T> generic used to return different types of converted inputs.
     */
    protected <T> T readFromInput (String prompt, Predicate<T> checker, Mapper<String, T> mapper, boolean acceptCommands){
        String commandChar = GameParameters.getCommandChar();
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print(prompt);
            boolean isInputAvailable = false;
            while(!isInputAvailable){
                try {
                    isInputAvailable = System.in.available() > 0;
                }catch (IOException e){
                    System.out.println(e.getMessage());
                }
            }
            if(Thread.interrupted()){
                return null;
            }
            String inputString = scanner.nextLine();
            boolean isCommand = acceptCommands && inputString.indexOf(GameParameters.getCommandChar()) == 0;
            if(!isCommand){
                try{
                    T mappedInput = mapper.apply(inputString);
                    if(checker.test(mappedInput)){
                        return mappedInput;
                    }
                }
                catch(MapperException e){
                    System.out.println("The supplied input's format isn't valid");
                }
                continue;
            }
            String[] splitString = inputString.split(" ", 2);
            String command = splitString[0];
            String argument = (splitString.length > 1) ? splitString[1] : "";
            checkCommand(command.substring(commandChar.length()), argument);
        }
    }

    /**
     * Method used to handle a client's command.
     * @param command the command to handle.
     * @param argument the arguments associated to the command.
     */
    protected abstract void checkCommand(String command, String argument);

    /**
     * Interface for mappers used in readFromInput.
     * If the conversion doesn't happen successfully, MapperException is thrown.
     * @param <T> the argument type
     * @param <U> the return type
     */
    @FunctionalInterface
    protected interface Mapper<T,U>{
        U apply(T t) throws MapperException;
    }

    /**
     * Method that converts a string to an integer.
     * @param input the string to convert.
     * @return the converted input.
     * @throws MapperException if the conversion isn't successful.
     */
    protected int stringToInt(String input) throws MapperException{
        try{
            return Integer.parseInt(input);
        }catch (NumberFormatException e){
            throw new MapperException();
        }
    }

    /**
     * Method that converts a string to an integer list.
     * @param input the string to convert.
     * @return the converted input.
     * @throws MapperException if the conversion isn't successful.
     */
    protected List<Integer> stringToListInt(String input) throws MapperException{
        try{
            List<Integer> result = new ArrayList<>();
            String[] numbersString = input.split(" ");
            for(String string : numbersString){
                result.add(Integer.parseInt(string));
            }
            return result;
        }catch(NumberFormatException e){
            throw new MapperException();
        }
    }

    /**
     * Method used to enable a string type return in the radFromInput method.
     * @param input the string to return.
     * @return the same string as the inputted one.
     */
    protected String stringIdentity(String input){
        return input;
    }

    /**
     * Tries to initialize the connection to the server. Could be a reconnection attempt.
     *
     * @return true if the connection is set without errors, false otherwise.
     */
    protected boolean tryConnect(ClientController currentController){
        try {
            ConnectionInitializer.initializeConnection(connectionSettings, currentController);
            new Thread(currentController).start();
            currentController.sendMessage(new Message(Status.REQUEST_PING));
            System.out.println(GameParameters.getTitle());
            return true;
        } catch (TCPException e) {
            System.out.println(e.getMessage());
        } catch (MalformedURLException e) {
            System.out.println("No RMI Server was found at the supplied address");
        } catch (NotBoundException e) {
            System.out.println("The requested object isn't bound");
        } catch (RemoteException e) {
            System.out.println("Couldn't connect to the RMI server");
        }
        return false;
    }

    /**
     * Shows that the client has disconnected. Tries to reconnect to the server.
     */
    public void disconnectionProcedure(SetupCLI setupCLI){
        controller.stop();
        ClientController newController = new ClientController(setupCLI, terminalSubmitter);
        do {
            readFromInput("\nOh no, your castings seem to not be received by us, codex gods... \nPress ENTER to try to reconnect",
                    (s) -> true, this::stringIdentity, false);
        } while (!tryConnect(newController));
        newController.sendMessage(getReconnectMessage());
        setupCLI.controller = newController;
    }

    /**
     * Gets the conventionally chosen message to send when the user disconnects in the current scene.
     *
     * @return the reconnect message to send.
     */
    protected abstract Message getReconnectMessage();
}