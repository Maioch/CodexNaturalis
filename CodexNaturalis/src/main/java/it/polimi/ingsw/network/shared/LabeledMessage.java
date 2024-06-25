package it.polimi.ingsw.network.shared;

import it.polimi.ingsw.network.shared.messages.Message;

/**
 * Represents a message along with its sender.
 *
 * @param exchangeHandler the handler of the client that sent the message.
 * @param message        the message itself.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 *
 * @see ExchangeHandler
 * @see Message
 */
public record LabeledMessage(ExchangeHandler exchangeHandler, Message message) {}