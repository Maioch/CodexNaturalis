package it.polimi.ingsw.view.cli;

import it.polimi.ingsw.exceptions.MapperException;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;

import java.util.*;
import java.util.function.Predicate;

public abstract class AbstractCLI {

    protected static final Map<Content, String> textColors = new HashMap<>(){{
        put(Content.RED, "\u001B[31m");
        put(Content.BLUE, "\u001B[34m");
        put(Content.GREEN, "\u001B[32m");
        put(Content.PURPLE, "\u001B[35m");
        put(Content.EMPTY, "\u001B[0m");
    }};

    protected <T> T readFromInput (String prompt, Predicate<T> checker, Mapper<String, T> mapper){
        String commandChar = GameParameters.getCommandChar();
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print(prompt);
            String inputString = scanner.nextLine();
            boolean isCommand = inputString.indexOf(GameParameters.getCommandChar()) == 0;
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

    protected abstract void checkCommand(String command, String argument);

    /**
     * Interface for mappers used in readInput
     * If the conversion doesn't happen successfully,
     * MapperException is thrown
     * @param <T> the argument type
     * @param <U> the return type
     */
    @FunctionalInterface
    protected interface Mapper<T,U>{
        U apply(T t) throws MapperException;
    }

    protected int stringToInt(String input) throws MapperException{
        try{
            return Integer.parseInt(input);
        }catch (NumberFormatException e){
            throw new MapperException();
        }
    }

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

    protected String stringIdentity(String input){
        return input;
    }
}