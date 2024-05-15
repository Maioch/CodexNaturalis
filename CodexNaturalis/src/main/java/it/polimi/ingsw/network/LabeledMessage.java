package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

/**
 * Record that represents a message along with its sender.
 * @param networkHandler the handler of the client that sent the message.
 * @param message the message itself.
 */
public record LabeledMessage(NetworkHandler networkHandler, Message message) {
}