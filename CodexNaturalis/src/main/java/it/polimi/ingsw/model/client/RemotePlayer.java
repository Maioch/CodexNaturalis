package it.polimi.ingsw.model.client;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardSides;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents one of the players connected to the same game as the local client.
 * It extends ClientPlayer and adds the player's hand cards, but only as a BasicCards array, as the only needed sides
 * are the ones that can be seen by the other players.
 *
 * @see BasicCard
 * @see ClientPlayer
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class RemotePlayer extends ClientPlayer {

    //the player's hand, which only consists of card backs.
    private List<BasicCard> handCards;

    /**
     * Class constructor.
     *
     * @param nickname the player's nickname.
     * @param color    the player's color.
     */
    public RemotePlayer(String nickname, Content color) {
        super(nickname, color);
        handCards = new ArrayList<>();
    }

    /**
     * Class copy-constructor.
     *
     * @param remotePlayer the instance to be copied.
     */
    public RemotePlayer(RemotePlayer remotePlayer) {
        super(remotePlayer);
        this.handCards = new ArrayList<>(remotePlayer.handCards);
    }

    /**
     * Sets the back sides cards in the player's hand and updates the view if the show flag is true.
     *
     * @param handCards the player's hand.
     * @param show      flag that determines whether to update the view.
     */
    @Override
    public synchronized void setHandCards(List<CardSides> handCards, boolean show) {
        this.handCards = handCards.stream()
                .map(CardSides::backSide)
                .collect(Collectors.toCollection(ArrayList::new));
        if(show){
            List<BasicCard> currentHandCards = getHandCards();
            eventSubmitter.submit(() -> gameView.updateRemotePlayerHand(getNickname(), currentHandCards));
        }
    }

    /**
     * Gets the back sides cards held by the player in his hand.
     *
     * @return the player's hand cards.
     */
    public synchronized List<BasicCard> getHandCards() {
        return new ArrayList<>() {{
            for (BasicCard card : handCards) {
                add(card.copy());
            }
        }};
    }

    /**
     * Equals method override to fit the method to this class.
     *
     * @param obj player checked.
     *
     * @return true if this player is equal to the parameter one.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RemotePlayer other){
            return this.getNickname().equals(other.getNickname());
        }
        return false;
    }
}