package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;

/**
 * CardAssetsProvider
 */
public class CardAssetsProvider {

    private static final String frontPath = "/";
    private static final String backPath = "/";
    private static final String objectivesPath = "/";

    /**
     * Returns the path where the parameter card is saved.
     * Resource and gold cards have the same back side, except for the color: if the card isn't a starter and its back
     * side is requested, the returned ID is the same for every card with the same color.
     *
     * @param card the card requested
     * @return     the path the card is saved in.
     */
    public static String getCardFilePath(BasicCard card){
        int id = card.getCardId();
        boolean isFront = card.isFront();
        if(id < GameParameters.getStartCardIndex(CardType.STARTER) || id > GameParameters.getEndCardIndex(CardType.STARTER)){
            id /= (GameParameters.getEndCardIndex(CardType.RESOURCE) - GameParameters.getStartCardIndex(CardType.RESOURCE));
        }
        return isFront ? frontPath : backPath + id + ".png";
    }

    /**
     * Returns the path where the parameter objective is saved.
     *
     * @param objective the objective requested
     * @return          the path the objective is saved in.
     */
    public static String getObjectiveFilePath(Objective objective){
        return objectivesPath + objective.getObjectiveId() + ".png";
    }
}