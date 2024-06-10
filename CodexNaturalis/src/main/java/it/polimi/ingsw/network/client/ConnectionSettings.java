package it.polimi.ingsw.network.client;

public record ConnectionSettings(String ip, int port, ConnectionType type) {
    public enum ConnectionType{
        TCP, RMI;
    }
}