package it.polimi.ingsw.network;

import it.polimi.ingsw.network.messages.Message;

/**
 * Record that represents a message with its sender network handler
 * @param networkHandler sender network handler
 * @param message message
 */
public record LabeledMessage(NetworkHandler networkHandler, Message message) {
}