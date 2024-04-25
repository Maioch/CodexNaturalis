package it.polimi.ingsw.network.server;

import java.io.IOException;
import java.net.ServerSocket;

public class SocketImplementation implements Server {
    private final ServerSocket serverSocket;

    public SocketImplementation(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    @Override
    public void requestColor() {

    }
}