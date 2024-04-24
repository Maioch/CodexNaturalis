package it.polimi.ingsw.controller;

import it.polimi.ingsw.exceptions.*;
import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.GameModel;
import it.polimi.ingsw.model.Player;
import it.polimi.ingsw.model.card.BasicCard;
import it.polimi.ingsw.model.card.CardSides;
import it.polimi.ingsw.model.card.CardType;
import it.polimi.ingsw.model.card.corner.Corner;

import java.util.List;

/**
 * Class that represents the controller for each game, according to the MVC model.
 *
 * @author Andrea Fidanza, Guglielmo Gatti, Marco Maiocchi, Francesco Saverio Nisoli
 */
public class GameController {
    private final GameModel game;

    /**
     * Constructor for the class
     * @param firstPlayerName the player that creates a game and the first to be added
     * @param numberOfPlayers the number of players allowed to enter the game and needed for it to start
     * @throws IllegalNumberOfPlayers when the number of players chosen exceeds the limit
     */
    public GameController(String firstPlayerName, int numberOfPlayers) throws IllegalNumberOfPlayers{
        this.game = new GameModel(numberOfPlayers);
    }

    /**
     * Method that adds a new player to the game, places his starter card and, if the game is full, it starts
     * @param nickname the username of the new player
     * @throws GameException when the color chosen by the new player was already taken
     * @throws GameFullException when the game is full
     * @throws NicknameTakenException when the username of the new player already exists in the game
     */
    public void acceptPlayer(String nickname) throws GameException, GameFullException, NicknameTakenException{
        //player chooses color
        Content chosenColor = Content.BLUE;
        CardSides starter = game.addPlayer(nickname, chosenColor);
        //shows the starter card to the player
        game.getPlayer(nickname).placeStarterCard(starter.frontSide());
        if(game.isGameFull()){
            startGame();
        }
    }

    /**
     * Core method that runs the match
     */
    private void startGame(){
        while(!game.isLastTurn()){
            for(Player player : game.getAllPlayers()){
                placeCard(player);
            }
        }
        //last turn of the game
        for(Player player : game.getAllPlayers()){
            placeCard(player);
        }
        List<String> winners = game.getWinningPlayers();
        //ViewUpdater is updated with the winners
    }

    /**
     * Method used to place a card for a player
     * @param player the player that plays the card
     */
    private void placeCard(Player player){
        //the player chooses the card to place
        BasicCard cardToPlace = null;
        while(!player.checkRequirements(cardToPlace) || !player.isCardInHand(cardToPlace)){
            //the player chooses the card to place
            cardToPlace = null;
        }
        //the player chooses the corner
        Corner chosenCorner = null;
        while(!player.checkIfPlaceable(chosenCorner) || !player.isCornerPartOfBoard(chosenCorner)){
            //the player chooses the corner
            chosenCorner = null;
        }
        player.placeCard(cardToPlace, chosenCorner);
    }

    /**
     * Method used to give the player a new card
     * @param player the player that is drawing
     */
    private void drawCard(Player player){
        //the player chooses the card to draw
        CardType typeChosen = CardType.RESOURCE;
        int indexChosen = 0;
        boolean drawSuccess = false;
        while(!drawSuccess){
            try{
                drawSuccess = true;
                game.drawCard(player, typeChosen, indexChosen);
            }catch(GameException e){
                drawSuccess = false;
            }
        }
    }
}