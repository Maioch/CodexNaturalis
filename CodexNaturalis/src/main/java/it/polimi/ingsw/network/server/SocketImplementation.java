package it.polimi.ingsw.network.server;

import it.polimi.ingsw.server.model.Content;
import it.polimi.ingsw.server.model.card.BasicCard;
import it.polimi.ingsw.server.model.card.CardSides;
import it.polimi.ingsw.server.model.card.CardType;
import it.polimi.ingsw.server.model.card.Objective;
import it.polimi.ingsw.server.model.card.corner.Corner;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class SocketImplementation implements DeprecatedServerListener {
    private final Socket socket;
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public SocketImplementation(Socket socket) throws IOException{
        this.socket = socket;
        this.inputStream = socket.getInputStream();
        this.outputStream = socket.getOutputStream();
    }

    @Override
    public Content requestColor(){
        return null;
    }

    @Override
    public BasicCard requestCardToPlace(){
        return null;
    }

    @Override
    public Corner requestCornerToPlaceOn(){
        return null;
    }

    @Override
    public BasicCard requestStarterSide(){
        return null;
    }

    @Override
    public Point requestCardToDraw(){
        return null;
    }

    @Override
    public void sendObjectives(ArrayList<Objective> objectives){

    }

    @Override
    public void sendHandCards(ArrayList<CardSides> handCards){

    }

    @Override
    public void sendBoard(ArrayList<BasicCard> board){

    }

    @Override
    public void sendStarterCard(CardSides starter){

    }

    @Override
    public void sendDrawableCards(HashMap<CardType, ArrayList<BasicCard>> drawableCards){

    }
}