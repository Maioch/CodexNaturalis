package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.exceptions.MapperException;
import it.polimi.ingsw.exceptions.TCPException;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.core.Client;
import it.polimi.ingsw.network.client.ConnectionInitializer;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;

import java.io.IOException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Predicate;

/**
 * Contains all the methods used to handle the client's inputs.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public abstract class AbstractCLI {
    /**
     * Reads the client's inputs.
     *
     * @param <T>            generic used to return different types of converted inputs.
     * @param prompt         the message printed to ask the client to input something.
     * @param checker        predicate that checks if the client's input is valid.
     * @param mapper         mapper that converts the client's input into the correct requested type.
     * @param acceptCommands whether commands should be accepted or not
     *
     * @return        the converted client's input.
     *
     * @see Mapper
     */
    protected <T> T readFromInput (String prompt, Predicate<T> checker, Mapper<String, T> mapper, boolean acceptCommands){
        String commandChar = Parameters.getCommandChar();
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
            boolean isCommand = acceptCommands && inputString.indexOf(Parameters.getCommandChar()) == 0;
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
     * Used to handle a client's command.
     *
     * @param command  the command to handle.
     * @param argument the command's arguments.
     */
    protected abstract void checkCommand(String command, String argument);

    /**
     * Used to convert a given value to another type in readFromInput.
     * If the conversion doesn't happen successfully, MapperException is thrown.
     *
     * @param <T> the argument type
     * @param <U> the return type
     */
    @FunctionalInterface
    protected interface Mapper<T,U>{
        U apply(T t) throws MapperException;
    }

    /**
     * Converts a string to an integer.
     *
     * @param input            the string to convert.
     *
     * @return                 the converted input.
     *
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
     * Converts a string to an integer list.
     *
     * @param input            the string to convert.
     *
     * @return                 the converted input.
     *
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
     * Used whenever readFromInput needs to return a string.
     *
     * @param input the string to return.
     *
     * @return      the same string as the inputted one.
     */
    protected String stringIdentity(String input){
        return input;
    }

    /**
     * Tries to initialize the connection to the server. Could be a reconnection attempt.
     *
     * @param client the client that stores this session's connection settings and controller.
     *
     * @return       true if the connection is set without errors, false otherwise.
     *
     * @see Client
     */
    protected boolean tryConnect(Client client){
        try {
            ConnectionInitializer.initializeConnection(client.getConnectionSettings(), client.getController());
            new Thread(client.getController()).start();
            client.getController().sendMessage(new Message(Status.REQUEST_PING));
            System.out.println(Parameters.getTitle());
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
     *
     * @param client the client that keeps track of the current controller.
     *
     * @see Client
     */
    protected void disconnectionProcedure(Client client){
        client.getController().stop();
        client.createController();
        do {
            readFromInput("\nOh no, your castings seem to not be received by us, codex gods... \nPress ENTER to try to reconnect",
                    (s) -> true, this::stringIdentity, false);
        } while (!tryConnect(client));
    }
}