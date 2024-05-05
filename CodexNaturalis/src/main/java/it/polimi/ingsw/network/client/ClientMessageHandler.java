package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.client.ClientGame;
import it.polimi.ingsw.model.client.ClientPlayer;
import it.polimi.ingsw.model.client.LocalPlayer;
import it.polimi.ingsw.model.client.RemotePlayer;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.*;
import it.polimi.ingsw.network.messages.generic.ContentMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.messages.setup.PlayerMessage;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

public class ClientMessageHandler extends MessageHandler{
    ClientGame game;
    SetupView setupView;
    GameView gameView;
    NetworkHandler networkHandler;
    EventSubmitter eventSubmitter;

    public ClientMessageHandler(ClientGame game, SetupView setupView, EventSubmitter eventSubmitter) {
        this.game = game;
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
    }

    public void sendMessage(Message message){
        networkHandler.update(message);
    }

    public synchronized void setGameView(GameView gameView){
        this.gameView = gameView;
    }

    @Override
    public void run(){
        while(true){
            LabeledMessage labeledMessage = getMessageFromQueue();
            if(labeledMessage == null){
                continue;
            }
            if(game == null){
                switch (labeledMessage.message().getStatus()){
                    case REQUEST_GAMES -> {
                        if(labeledMessage.message() instanceof MatchListMessage matchListMessage){
                            eventSubmitter.submit(() -> setupView.updateMatchList(matchListMessage.getMatchList()));
                        }
                    }
                    case NEW_GAME -> {
                        if(labeledMessage.message() instanceof IntegerMessage integerMessage){
                            eventSubmitter.submit(() -> setupView.newGameSuccess(integerMessage.getValue()));
                            networkHandler.update(new IntegerMessage(Status.REQUEST_COLORS, integerMessage.getValue()));
                        }
                    }
                    case INVALID_PLAYERS_NUMBER -> eventSubmitter.submit(() -> setupView.showCriticalError(Status.INVALID_PLAYERS_NUMBER.getMessage()));
                    case REQUEST_COLORS -> {
                        if(labeledMessage.message() instanceof ContentMessage contentMessage){
                             eventSubmitter.submit(contentMessage.getContent().isEmpty() ?
                                     () -> setupView.showCriticalError(Status.GAME_FULL.getMessage()) :
                                     () -> setupView.showJoinGameDialog(contentMessage.getContent()));
                        }
                    }
                    case JOIN_GAME -> {
                        if(labeledMessage.message() instanceof PlayerMessage playerMessage){
                            eventSubmitter.submit(() -> setupView.showSuccessfulJoin());
                            synchronized (this) {
                                game = new ClientGame(new LocalPlayer(playerMessage.getNickname(), playerMessage.getColor()),
                                        eventSubmitter,
                                        gameView);
                            }
                        }
                    }
                    case GAME_FULL -> eventSubmitter.submit(() -> setupView.showCriticalError(Status.GAME_FULL.getMessage()));
                    case INVALID_NICKNAME -> eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_NICKNAME.getMessage()));
                    case INVALID_COLOR -> eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_COLOR.getMessage()));
                }
                continue;
            }
            synchronized (this) {
                switch (labeledMessage.message().getStatus()) {
                    case NEW_PLAYER_JOINED -> {
                        if (labeledMessage.message() instanceof PlayerMessage playerMessage) {
                            boolean isPlayerMissing = game.getRemotePlayers().stream()
                                    .map(RemotePlayer::getNickname)
                                    .noneMatch(n -> n.equals(playerMessage.getNickname()));
                            if (isPlayerMissing) {
                                game.addRemotePlayer(new RemotePlayer(playerMessage.getNickname(), playerMessage.getColor()));
                            }
                        }
                    }
                    case TURN_NOTIFICATION -> {
                        if (labeledMessage.message() instanceof StringMessage stringMessage) {
                            game.setPlayerWithTurn(stringMessage.getString());
                        }
                    }
                    case DRAW_OPTIONS -> {
                        if (labeledMessage.message() instanceof DrawOptionsMessage drawOptionsMessage) {
                            game.setDrawableOptions(drawOptionsMessage.getDrawableOptions());
                        }
                    }
                    case STARTER_CARD, INVALID_STARTER_CARD -> {
                        if (labeledMessage.message().getStatus() == Status.INVALID_STARTER_CARD) {
                            eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_STARTER_CARD.getMessage()));
                        }
                        if (labeledMessage.message() instanceof CardHandMessage cardHandMessage) {
                            game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand());
                        }
                    }
                    case OBJECTIVES -> {
                        if (labeledMessage.message() instanceof ObjectivesMessage objectivesMessage) {
                            game.setCommonObjectives(objectivesMessage.getCommonObjectives());
                            game.getLocalPlayer().setPersonalObjectives(objectivesMessage.getPersonalObjectives());
                        }
                    }
                    case PLACE_CARD, INVALID_PLACE_CARD -> {
                        if (labeledMessage.message().getStatus() == Status.INVALID_PLACE_CARD) {
                            eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_PLACE_CARD.getMessage()));
                        }
                        if (labeledMessage.message() instanceof ValidPlacementsMessage validPlacementsMessage) {
                            eventSubmitter.submit(() -> gameView.requestPlacement(
                                    validPlacementsMessage.getPlaceableCards(),
                                    validPlacementsMessage.getPlaceableCorners()));
                        }
                    }
                    case PLACEMENT_OK -> {
                        if (labeledMessage.message() instanceof PlayerBoardMessage playerBoardMessage) {
                            ClientPlayer playerWithTurn = game.getPlayerWithTurn();
                            playerWithTurn.setPlacedCards(playerBoardMessage.getBoard());
                            playerWithTurn.setScore(playerBoardMessage.getPlayerScore());
                        }
                    }
                    case PLAYER_HAND_CARDS -> {
                        if (labeledMessage.message() instanceof CardHandMessage cardHandMessage) {
                            game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand());
                        }
                    }
                    case PLAYER_HAND_BACK -> {
                        if (labeledMessage.message() instanceof CardHandMessage cardHandMessage) {
                            game.getPlayerWithTurn().setHandCards(cardHandMessage.getCardHand());
                        }
                    }
                    case DRAW, INVALID_DRAW -> {
                        if (labeledMessage.message().getStatus() == Status.INVALID_DRAW) {
                            eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_DRAW.getMessage()));
                        }
                        if (labeledMessage.message() instanceof DrawOptionsMessage drawOptionsMessage) {
                            game.setDrawableOptions(drawOptionsMessage.getDrawableOptions());
                            eventSubmitter.submit(() -> gameView.requestDraw());
                        }
                    }
                    case LAST_TURN -> eventSubmitter.submit(() -> gameView.notifyLastTurn());
                    case PLAYER_FINAL_SCORE -> {
                        if (labeledMessage.message() instanceof PlayerSummaryMessage playerSummaryMessage) {
                            game.getPlayerWithTurn().setFinalScore(
                                    playerSummaryMessage.getObjectiveScores(), playerSummaryMessage.getFinalScore());
                        }
                    }
                    case DECLARE_WINNER -> {
                        if (labeledMessage.message() instanceof WinnersMessage winnersMessage) {
                            eventSubmitter.submit(() -> gameView.showGameEndScreen(winnersMessage.getWinners()));
                        }
                    }
                    case CHAT -> {
                        if (labeledMessage.message() instanceof ChatMessage chatMessage) {
                            eventSubmitter.submit(() -> gameView.showChatMessage(
                                    chatMessage.getMessage(),
                                    chatMessage.getSender(),
                                    chatMessage.getRecipients()));
                        }
                    }
                }
            }
        }
    }
}