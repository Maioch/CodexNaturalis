package it.polimi.ingsw.network.server;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketImplementation implements ServerListener {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SocketImplementation(Socket socket) throws IOException {
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public Content requestColor() {
        return null;
    }

    @Override
    public BasicCard requestCardToPlace() {
        return null;
    }

    @Override
    public Corner requestCornerToPlaceOn() {
        return null;
    }

    @Override
    public BasicCard requestStarterSide() {
        return null;
    }

    @Override
    public Point requestCardToDraw() {
        return null;
    }

    @Override
    public void sendObjectives(ArrayList<Objective> objectives) {

    }

    @Override
    public void sendHandCards(ArrayList<CardSides> handCards) {

    }

    @Override
    public void sendBoard(ArrayList<BasicCard> board) {

    }

    @Override
    public void sendStarterCard(CardSides starter) {

    }

    @Override
    public void sendDrawableCards(HashMap<CardType, ArrayList<BasicCard>> drawableCards) {

    }
}