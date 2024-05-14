package it.polimi.ingsw.network.client;

import it.polimi.ingsw.model.client.ClientGame;
import it.polimi.ingsw.model.client.ClientPlayer;
import it.polimi.ingsw.model.client.LocalPlayer;
import it.polimi.ingsw.model.client.RemotePlayer;
import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.Objective;
import it.polimi.ingsw.network.LabeledMessage;
import it.polimi.ingsw.network.MessageHandler;
import it.polimi.ingsw.network.NetworkHandler;
import it.polimi.ingsw.network.messages.*;
import it.polimi.ingsw.network.messages.game.*;
import it.polimi.ingsw.network.messages.setup.GameColorsMessage;
import it.polimi.ingsw.network.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.messages.generic.StringMessage;
import it.polimi.ingsw.network.messages.setup.MatchListMessage;
import it.polimi.ingsw.network.messages.setup.PlayerMessage;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

import java.util.Map;
import java.util.List;

/**
 * Class that handles every possible message the client can send to the server.
 */
public class ClientController extends MessageHandler{
    private final SetupView setupView;
    private ClientGame game;
    private volatile GameView gameView;
    private NetworkHandler networkHandler;
    private final EventSubmitter eventSubmitter;

    /**
     * Constructor for the class.
     * @param setupView the object containing all the methods used by the player to access or create a game.
     * @param eventSubmitter the medium used to send the player's requests to the server.
     */
    public ClientController(SetupView setupView, EventSubmitter eventSubmitter) {
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
    }

    /**
     * Method that lets the client handler send a specified message.
     * @param message the message the client is sending.
     */
    public void sendMessage(Message message){
        networkHandler.update(message);
    }

    /**
     * Setter for the game view attribute.
     * @param gameView the interface associated to the client.
     */
    public synchronized void setGameView(GameView gameView){
        this.gameView = gameView;
    }

    /**
     * Setter for the network handler attribute.
     * @param networkHandler the handler associated to the client.
     */
    public void setNetworkHandler(NetworkHandler networkHandler){
        this.networkHandler = networkHandler;
    }

    /**
     * @return the client's player nickname.
     */
    public String getLocalPlayerName(){
        return game.getLocalPlayer().getNickname();
    }

    /**
     * @return all other player's nicknames.
     */
    public List<String> getRemotePlayerNames(){
        return game.getRemotePlayers().stream().map(RemotePlayer::getNickname).toList();
    }

    /**
     * @return the client's player current board.
     */
    public List<BasicCard> getLocalPlayerBoard(){
        return game.getLocalPlayer().getPlacedCards();
    }

    /**
     * @param nickname the nickname of a remote player.
     * @return the specified player's board.
     */
    public List<BasicCard> getRemotePlayerBoard(String nickname){
        return game.getRemotePlayers().stream()
                .filter(p -> p.getNickname()
                        .equals(nickname)).findFirst().orElseThrow().getPlacedCards();
    }

    public Map<String, Content> getPlayerColors(){
        return game.getPlayersColors();
    }

    public List<Objective> getCommonObjectives() { return game.getCommonObjectives(); }

    public List<Objective> getPersonalObjectives() {return game.getLocalPlayer().getPersonalObjectives(); }


    public synchronized void backToSetup() {
        eventSubmitter.submit(() -> gameView.closeView());
        this.game = null;
        this.gameView = null;
        sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * Main method of the class, used to differentiate between all the possible messages the client can send.
     */
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
                        }
                    }
                    case INVALID_PLAYERS_NUMBER -> eventSubmitter.submit(() -> setupView.showCriticalError(Status.INVALID_PLAYERS_NUMBER.getMessage()));
                    case REQUEST_COLORS -> {
                        if(labeledMessage.message() instanceof GameColorsMessage gameColorsMessage){
                             eventSubmitter.submit(gameColorsMessage.getContent().isEmpty() ?
                                     () -> setupView.showCriticalError(Status.GAME_FULL.getMessage()) :
                                     () -> setupView.showJoinGameDialog(gameColorsMessage.getContent(), gameColorsMessage.getGameId()));
                        }
                    }
                    case JOIN_GAME -> {
                        if(labeledMessage.message() instanceof PlayerMessage playerMessage){
                            eventSubmitter.submit(setupView::showSuccessfulJoin);
                            //TODO: PREVENT RACE CONDITION BY ADDING VIEWREADY MESSAGE
                            while (gameView == null) Thread.onSpinWait();
                            synchronized (this) {
                                game = new ClientGame(
                                        new LocalPlayer(playerMessage.getNickname(), playerMessage.getColor()),
                                        eventSubmitter,
                                        gameView);
                            }
                        }
                    }
                    case GAME_FULL -> eventSubmitter.submit(() -> setupView.showCriticalError(Status.GAME_FULL.getMessage()));
                    case INVALID_NICKNAME -> {
                        if(labeledMessage.message() instanceof IntegerMessage integerMessage) {
                            eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_NICKNAME.getMessage(), integerMessage.getValue()));
                        }
                    }
                    case INVALID_COLOR -> {
                        if(labeledMessage.message() instanceof IntegerMessage integerMessage) {
                            eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_COLOR.getMessage(), integerMessage.getValue()));
                        }
                    }
                }
                continue;
            }
            synchronized (this) {
                switch (labeledMessage.message().getStatus()) {
                    case NEW_PLAYER_JOINED -> {
                        if (labeledMessage.message() instanceof PlayerMessage playerMessage) {
                            boolean isPlayerMissing = game.getRemotePlayers().stream()
                                    .map(RemotePlayer::getNickname)
                                    .noneMatch(n -> n.equals(playerMessage.getNickname())) &&
                                    !getLocalPlayerName().equals(playerMessage.getNickname());
                            if (isPlayerMissing) {
                                game.addRemotePlayer(new RemotePlayer(
                                        playerMessage.getNickname(), playerMessage.getColor()));
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
                            game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand(), false);
                            game.getLocalPlayer().requestStarterCardPlacement();
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
                            game.getLocalPlayer().requestCardPlacement(
                                    validPlacementsMessage.getPlaceableCards(),
                                    validPlacementsMessage.getPlaceableCorners());
                        }
                    }
                    case PLACEMENT_OK -> {
                        if (labeledMessage.message() instanceof PlayerBoardMessage playerBoardMessage) {
                            ClientPlayer playerWithTurn = game.getPlayerWithTurn();
                            playerWithTurn.setPlacedCards(playerBoardMessage.getBoard(), playerBoardMessage.getPlayerScore());
                        }
                    }
                    case PLAYER_HAND_CARDS -> {
                        if (labeledMessage.message() instanceof CardHandMessage cardHandMessage) {
                            game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand(), true);
                        }
                    }
                    case PLAYER_HAND_BACK -> {
                        if (labeledMessage.message() instanceof CardHandMessage cardHandMessage) {
                            if(game.getPlayerWithTurn() != game.getLocalPlayer()){
                                game.getPlayerWithTurn().setHandCards(cardHandMessage.getCardHand(), true);
                            }
                        }
                    }
                    case DRAW, INVALID_DRAW -> {
                        if (labeledMessage.message().getStatus() == Status.INVALID_DRAW) {
                            eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_DRAW.getMessage()));
                        }
                        if (labeledMessage.message() instanceof DrawOptionsMessage drawOptionsMessage) {
                            game.requestDraw(drawOptionsMessage.getDrawableOptions());
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
                            eventSubmitter.submit(() -> gameView.revealWinners(winnersMessage.getWinners()));
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