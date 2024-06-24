package it.polimi.ingsw.network.client;

/**
 * Represents the settings chosen in the initial (connection) setup phase by the client user.
 *
 * @param ip   the server's ip.
 * @param port the server's port.
 * @param type the connection type (either TCP or RMI).
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public record ConnectionSettings(String ip, int port, ConnectionType type) {

    /**
     * Represents the connection type.
     */
    public enum ConnectionType{

        /**
         * Connection using TCP.
         */
        TCP,

        /**
         * Connection using RMI.
         */
        RMI
    }
}