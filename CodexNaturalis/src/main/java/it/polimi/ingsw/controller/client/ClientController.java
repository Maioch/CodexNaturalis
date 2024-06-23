package it.polimi.ingsw.controller.client;

import it.polimi.ingsw.model.client.ClientGame;
import it.polimi.ingsw.model.client.ClientPlayer;
import it.polimi.ingsw.model.client.LocalPlayer;
import it.polimi.ingsw.model.client.RemotePlayer;
import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;
import it.polimi.ingsw.model.shared.card.Objective;
import it.polimi.ingsw.model.shared.card.corner.Corner;
import it.polimi.ingsw.network.shared.EventHandler;
import it.polimi.ingsw.network.shared.LabeledMessage;
import it.polimi.ingsw.network.shared.NetworkHandler;
import it.polimi.ingsw.network.shared.messages.Message;
import it.polimi.ingsw.network.shared.messages.Status;
import it.polimi.ingsw.network.shared.messages.game.*;
import it.polimi.ingsw.network.shared.messages.generic.IntegerMessage;
import it.polimi.ingsw.network.shared.messages.generic.StringMessage;
import it.polimi.ingsw.network.shared.messages.setup.GameColorsMessage;
import it.polimi.ingsw.network.shared.messages.setup.JoinGameMessage;
import it.polimi.ingsw.network.shared.messages.setup.MatchListMessage;
import it.polimi.ingsw.view.EventSubmitter;
import it.polimi.ingsw.view.GameView;
import it.polimi.ingsw.view.SetupView;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Class that handles every possible message the client can send to the server.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class ClientController extends EventHandler<LabeledMessage> {

    private final SetupView setupView;
    private ClientGame game;
    private GameView gameView;
    private NetworkHandler networkHandler;
    private final Object networkHandlerLock;
    private final EventSubmitter eventSubmitter;
    private final AtomicBoolean isDisconnected;
    private Thread controllerThread;
    private final Timer pingTimer;

    /**
     * Class constructor.
     *
     * @param setupView      the object containing all the methods used by the player to access or create a game.
     * @param eventSubmitter the medium used to send the player's requests to the server.
     */
    public ClientController(SetupView setupView, EventSubmitter eventSubmitter) {
        this.pingTimer = new Timer();
        this.setupView = setupView;
        this.eventSubmitter = eventSubmitter;
        this.networkHandlerLock = new Object();
        isDisconnected = new AtomicBoolean(false);
    }

    /**
     * Lets the client handler send a specified message.
     *
     * @param message the message the client is sending.
     */
    public void sendMessage(Message message){
        synchronized (networkHandlerLock) {
            networkHandler.update(message);
        }
    }

    /**
     * Sets the game view attribute and notifies the "this" object.
     *
     * @param gameView the interface associated to the client.
     */
    public synchronized void setGameView(GameView gameView){
        this.gameView = gameView;
        notifyAll();
    }

    /**
     * Sets the network handler attribute.
     *
     * @param networkHandler the handler associated to the client.
     */
    public void setNetworkHandler(NetworkHandler networkHandler){
        synchronized (networkHandlerLock) {
            this.networkHandler = networkHandler;
        }
    }

    /**
     * Gets the local player's nickname.
     *
     * @return the local player's nickname.
     */
    public synchronized String getLocalPlayerName(){
        return game.getLocalPlayer().getNickname();
    }

    /**
     * Gets all the remote players' nickname.
     *
     * @return all the remote players' nicknames.
     */
    public synchronized List<String> getRemotePlayerNames(){
        return game.getRemotePlayers().stream().map(RemotePlayer::getNickname).toList();
    }

    /**
     * Gets the local player's current board.
     *
     * @return the local player's current board.
     */
    public synchronized List<BasicCard> getLocalPlayerBoard(){
        return game.getLocalPlayer().getPlacedCards();
    }

    /**
     * Gets the specified remote player's board.
     *
     * @param nickname the nickname of the remote player.
     * @return         the specified player's board.
     */
    public synchronized List<BasicCard> getRemotePlayerBoard(String nickname){
        return game.getRemotePlayers().stream()
                .filter(p -> p.getNickname()
                        .equals(nickname)).findFirst().orElseThrow().getPlacedCards();
    }

    /**
     * Gets the local player's current placeable cards.
     *
     * @return the local player's current placeable cards.
     */
    public synchronized List<BasicCard> getLocalPlayerValidCards(){
        return game.getLocalPlayer().getValidCards();
    }

    /**
     * Gets the local player's current valid corners.
     *
     * @return the local player's current valid corners.
     */
    public synchronized List<Corner> getLocalPlayerValidCorners(){
        return game.getLocalPlayer().getValidCorners();
    }

    /**
     * Gets all the players' colors.
     *
     * @return a map that contains each player's nickname (key), and it's color.
     */
    public synchronized Map<String, Content> getPlayerColors(){
        return game.getPlayerColors();
    }

    /**
     * Gets the local player's common objectives.
     *
     * @return the local player's common objectives.
     */
    public synchronized List<Objective> getCommonObjectives() { return game.getCommonObjectives(); }

    /**
     * Gets the local player's personal objectives.
     *
     * @return the local player's personal objectives.
     */
    public synchronized List<Objective> getPersonalObjectives() {return game.getLocalPlayer().getPersonalObjectives(); }

    /**
     * Gets the nickname of the player with the turn.
     *
     * @return the nickname of the player with the turn.
     */
    public synchronized String getPlayerWithTurn() { return game.getPlayerWithTurn().getNickname(); }

    /**
     * Gets the game's id.
     *
     * @return the game's id.
     */
    public synchronized int getGameId() { return game.getGameId(); }

    /**
     * Gets the specified remote player's hand.
     *
     * @param nickname the remote player's nickname.
     * @return         the specified player's hand (back sides only).
     */
    public synchronized List<BasicCard> getRemotePlayerHand(String nickname) {
        return game.getRemotePlayers().stream()
                .filter(p -> p.getNickname().equals(nickname))
                .findFirst().map(RemotePlayer::getHandCards)
                .orElse(new ArrayList<>());
    }

    /**
     * Gets the local player's hand.
     *
     * @return the local player's card hand
     */
    public synchronized List<CardSides> getLocalPlayerHand() {
        return game.getLocalPlayer().getHandCards();
    }

    /**
     * Sets both the client game model and the game view to null.
     */
    public synchronized void backToSetup() {
        this.game = null;
        this.gameView = null;
        sendMessage(new Message(Status.REQUEST_GAMES));
    }

    /**
     * It starts the ping's thread for detecting disconnections and handles messages received on the queue until the
     * current thread (controller thread) is interrupted.
     */
    @Override
    public void run(){
        controllerThread = Thread.currentThread();
        int periodSeconds = Parameters.getClientPingPeriodSeconds();
        pingTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                handlePings();
            }
        }, periodSeconds * 1000L, periodSeconds * 1000L);
        while(!controllerThread.isInterrupted()) {
            LabeledMessage labeledMessage = getEventFromQueue();
            if (labeledMessage == null) {
                continue;
            }
            Message message = labeledMessage.message();
            synchronized (this) {
                if (message.getStatus() == Status.PING_ACK){
                    isDisconnected.set(false);
                }
                if (game == null) {
                    handleSetupMessage(message);
                } else {
                    handleGameMessage(message);
                }
            }
        }
        pingTimer.cancel();
    }

    /**
     * It interrupts the controller's thread.
     */
    public void stop(){
        controllerThread.interrupt();
    }

    /**
     * Handles the ping's procedure to detect disconnections. If the ping ack is not received, it notifies the view.
     */
    private void handlePings(){
        if(isDisconnected.get()){
            pingTimer.cancel();
            networkHandler.stop();
            eventSubmitter.submit(((gameView == null) ? setupView : gameView)::showDisconnectionMessage);
            return;
        }
        isDisconnected.set(true);
        sendMessage(new Message(Status.REQUEST_PING));
    }

    /**
     * Handles a setup message. It notifies the client model or the setup view depending on the message status.
     *
     * @param message the message to handle.
     */
    private void handleSetupMessage(Message message) {
        switch (message.getStatus()) {
            case REQUEST_GAMES -> {
                if (message instanceof MatchListMessage matchListMessage) {
                    eventSubmitter.submit(() -> setupView.updateMatchList(matchListMessage.getMatchList()));
                }
            }
            case NEW_GAME -> {
                if (message instanceof IntegerMessage integerMessage) {
                    eventSubmitter.submit(() -> setupView.newGameSuccess(integerMessage.getValue()));
                }
            }
            case INVALID_PLAYERS_NUMBER ->
                    eventSubmitter.submit(() -> setupView.showCriticalError(Status.INVALID_PLAYERS_NUMBER.getMessage()));
            case REQUEST_COLORS -> {
                if (message instanceof GameColorsMessage gameColorsMessage) {
                    eventSubmitter.submit(gameColorsMessage.getContent().isEmpty() ?
                            () -> setupView.showCriticalError(Status.GAME_FULL.getMessage()) :
                            () -> setupView.showJoinGameDialog(gameColorsMessage.getContent(), gameColorsMessage.getGameId()));
                }
            }
            case JOIN_GAME -> {
                if (message instanceof JoinGameMessage joinGameMessage) {
                    eventSubmitter.submit(() -> setupView.showSuccessfulJoin(
                            joinGameMessage.getNickname(), joinGameMessage.getColor(), joinGameMessage.getGameInfo()));
                    if (gameView == null) {
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    game = new ClientGame(
                            new LocalPlayer(joinGameMessage.getNickname(), joinGameMessage.getColor()),
                            eventSubmitter,
                            gameView,
                            joinGameMessage.getGameInfo(),
                            joinGameMessage.getGameId());
                }
            }
            case GAME_FULL ->
                    eventSubmitter.submit(() -> setupView.showCriticalError(Status.GAME_FULL.getMessage()));
            case INVALID_NICKNAME -> {
                if (message instanceof IntegerMessage integerMessage) {
                    eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_NICKNAME.getMessage(), integerMessage.getValue()));
                }
            }
            case INVALID_COLOR -> {
                if (message instanceof IntegerMessage integerMessage) {
                    eventSubmitter.submit(() -> setupView.showUserError(Status.INVALID_COLOR.getMessage(), integerMessage.getValue()));
                }
            }
            case WRONG_NAME ->
                    eventSubmitter.submit(() -> setupView.showCriticalError(Status.WRONG_NAME.getMessage()));
            case ERROR ->
                    eventSubmitter.submit(() -> setupView.showCriticalError("The lobby for this match timed out. Please create a new one."));
            case INVALID_RECONNECT ->
                    eventSubmitter.submit(() -> setupView.showReconnectionError(Status.INVALID_RECONNECT.getMessage()));
        }
    }

    /**
     * Handles a game message. It notifies the client model or the game view depending on the message status.
     *
     * @param message the message to handle.
     */
    private void handleGameMessage(Message message) {
        switch (message.getStatus()) {
            case NEW_PLAYER_JOINED -> {
                if (message instanceof JoinGameMessage joinGameMessage) {
                    if (game.getLocalPlayer().getNickname().equals(joinGameMessage.getNickname())) {
                        game.getLocalPlayer().setTurnNumber(joinGameMessage.getGameInfo());
                        break;
                    }
                    boolean isPlayerMissing = game.getRemotePlayers().stream()
                            .map(RemotePlayer::getNickname)
                            .noneMatch(n -> n.equals(joinGameMessage.getNickname()));
                    if (isPlayerMissing) {
                        RemotePlayer player = new RemotePlayer(joinGameMessage.getNickname(), joinGameMessage.getColor());
                        player.setTurnNumber(joinGameMessage.getGameInfo());
                        game.addRemotePlayer(player);
                    }
                }
            }
            case TURN_NOTIFICATION -> {
                if (message instanceof StringMessage stringMessage) {
                    game.setPlayerWithTurn(stringMessage.getString(), true);
                }
            }
            case SILENT_TURN_NOTIFICATION -> {
                if (message instanceof StringMessage stringMessage){
                    game.setPlayerWithTurn(stringMessage.getString(), false);
                }
            }
            case DRAW_OPTIONS -> {
                if (message instanceof DrawOptionsMessage drawOptionsMessage) {
                    game.setDrawableOptions(drawOptionsMessage.getDrawableOptions(), drawOptionsMessage.getNumberOfCardsLeft());
                }
            }
            case STARTER_CARD, INVALID_STARTER_CARD -> {
                if (message.getStatus() == Status.INVALID_STARTER_CARD) {
                    eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_STARTER_CARD.getMessage()));
                }
                if (message instanceof CardHandMessage cardHandMessage) {
                    game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand(), false);
                    game.getLocalPlayer().requestStarterCardPlacement();
                }
            }
            case COMMON_OBJECTIVES -> {
                if (message instanceof ObjectivesMessage objectivesMessage) {
                    game.setCommonObjectives(objectivesMessage.getObjectives());
                }
            }
            case REQUEST_SECRET_OBJECTIVES, INVALID_SECRET_OBJECTIVES -> {
                if (message.getStatus() == Status.INVALID_SECRET_OBJECTIVES) {
                    eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_SECRET_OBJECTIVES.getMessage()));
                }
                if (message instanceof ObjectivesMessage objectivesMessage) {
                    eventSubmitter.submit(() -> gameView.requestPersonalObjectivesChoice(objectivesMessage.getObjectives()));
                }
            }
            case SECRET_OBJECTIVES -> {
                if (message instanceof ObjectivesMessage objectivesMessage) {
                    game.getLocalPlayer().setPersonalObjectives(objectivesMessage.getObjectives());
                }
            }
            case PLACE_CARD, INVALID_PLACE_CARD -> {
                if (message.getStatus() == Status.INVALID_PLACE_CARD) {
                    eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_PLACE_CARD.getMessage()));
                }
                if (message instanceof ValidPlacementsMessage validPlacementsMessage) {
                    game.getLocalPlayer().requestCardPlacement(
                            validPlacementsMessage.getPlaceableCards(),
                            validPlacementsMessage.getPlaceableCorners());
                }
            }
            case NO_MOVES -> {
                String player = game.getPlayerWithTurn().getNickname();
                eventSubmitter.submit(() -> gameView.showNoMovesAvailable(player));
            }
            case PLACEMENT_OK -> {
                if (message instanceof PlayerBoardMessage playerBoardMessage) {
                    ClientPlayer playerWithTurn = game.getPlayerWithTurn();
                    playerWithTurn.setPlacedCards(playerBoardMessage.getBoard(), playerBoardMessage.getPlayerScore());
                }
            }
            case PLAYER_HAND_CARDS -> {
                if (message instanceof CardHandMessage cardHandMessage) {
                    game.getLocalPlayer().setHandCards(cardHandMessage.getCardHand(), true);
                }
            }
            case PLAYER_HAND_BACK -> {
                if (message instanceof CardHandMessage cardHandMessage) {
                    if (game.getPlayerWithTurn() != game.getLocalPlayer()) {
                        game.getPlayerWithTurn().setHandCards(cardHandMessage.getCardHand(), true);
                    }
                }
            }
            case DRAW, INVALID_DRAW -> {
                if (message.getStatus() == Status.INVALID_DRAW) {
                    eventSubmitter.submit(() -> gameView.showErrorMessage(Status.INVALID_DRAW.getMessage()));
                }
                if (message instanceof DrawOptionsMessage drawOptionsMessage) {
                    game.requestDraw(drawOptionsMessage.getDrawableOptions(), drawOptionsMessage.getNumberOfCardsLeft());
                }
            }
            case LAST_TURN -> eventSubmitter.submit(() -> gameView.notifyLastTurn());
            case PLAYER_FINAL_SCORE -> {
                if (message instanceof PlayerSummaryMessage playerSummaryMessage) {
                    game.getPlayerWithNickname(playerSummaryMessage.getPlayerName()).setFinalScore(
                            playerSummaryMessage.getObjectiveScores(),
                            playerSummaryMessage.getFinalScore());
                }
            }
            case GAME_TIMEOUT_STARTED -> eventSubmitter.submit(() -> gameView.notifyGameTimeout());
            case DECLARE_WINNER -> {
                if (message instanceof WinnersMessage winnersMessage) {
                    eventSubmitter.submit(() -> gameView.revealWinners(winnersMessage.getWinners()));
                }
            }
            case GAME_CANCELED -> eventSubmitter.submit(() -> gameView.notifyGameCanceled());
            case TURN_SKIPPED -> eventSubmitter.submit(() -> gameView.notifyTurnSkipped());
            case PLAYER_DISCONNECTED -> {
                if (message instanceof StringMessage stringMessage) {
                    Content playerColor = getPlayerColors().get(stringMessage.getString()) != null ?
                            getPlayerColors().get(stringMessage.getString()) :
                            Content.WHITE;
                    eventSubmitter.submit(() -> gameView.notifyRemotePlayerDisconnected(stringMessage.getString(), playerColor));
                }
            }
            case PLAYER_LEFT_LOBBY -> {
                if (message instanceof StringMessage stringMessage) {
                    if(stringMessage.getString().equals(game.getLocalPlayer().getNickname())) {
                        backToSetup();
                        break;
                    }
                    game.removeRemotePlayer(stringMessage.getString());
                }
            }
            case RECONNECT -> {
                if (message instanceof StringMessage stringMessage) {
                    eventSubmitter.submit(() -> gameView.notifyRemotePlayerReconnected(stringMessage.getString()));
                }
            }
            case CHAT -> {
                if (message instanceof ChatMessage chatMessage) {
                    eventSubmitter.submit(() -> gameView.showChatMessage(chatMessage));
                }
            }
            case REQUEST_PING -> sendMessage(new Message(Status.PING_ACK));
        }
    }
}