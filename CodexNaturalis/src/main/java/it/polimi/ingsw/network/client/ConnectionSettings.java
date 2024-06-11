package it.polimi.ingsw.network.client;

/**
 * Record representing the settings chosen in the initial (connection) setup phase by the client user.
 *
 * @param ip the server's ip.
 * @param port the server's port.
 * @param type the connection type (either TCP or RMI).
 */
public record ConnectionSettings(String ip, int port, ConnectionType type) {
    public enum ConnectionType{
        TCP, RMI;
    }
}