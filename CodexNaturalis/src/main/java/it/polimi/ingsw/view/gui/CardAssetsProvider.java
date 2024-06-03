package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.server.card.BasicCard;
import it.polimi.ingsw.model.server.card.CardType;
import it.polimi.ingsw.model.server.card.Objective;

import java.util.Arrays;
import java.util.Objects;

/**
 * CardAssetsProvider
 */
public class CardAssetsProvider {

    private static final String frontPath = "/scenes/images/cardFronts/";
    private static final String backPath = "/scenes/images/cardBacks/";
    private static final String objectivesPath = "/scenes/images/cardFronts/";
    private static final String hiddenCardPath = "/scenes/images/";

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
        int startIndex = GameParameters.getStartCardIndex(CardType.STARTER);
        int endIndex = GameParameters.getEndCardIndex(CardType.STARTER);
        if(!isFront && (id < startIndex || id > endIndex)){
            // noinspection SuspiciousIntegerDivAssignment
            id /= ((GameParameters.getEndCardIndex(CardType.RESOURCE) -
                    GameParameters.getStartCardIndex(CardType.RESOURCE) + 1) /
                    Arrays.stream(Content.values()).filter(Content::isResource).mapToInt(c -> 1).sum());
        }
        return "file:" + (Objects.requireNonNull(CardAssetsProvider.class.getResource(
                (isFront ? frontPath : backPath) + id + ".png"))).getFile();
    }

    /**
     * Returns the path where the parameter objective is saved.
     *
     * @param objective the objective requested
     * @return          the path the objective is saved in.
     */
    public static String getObjectiveFilePath(Objective objective){
        return "file:" + (Objects.requireNonNull(CardAssetsProvider.class.getResource(
                objectivesPath + objective.getObjectiveId() + ".png"))).getFile();
    }

    public static String getHiddenCardFilePath(){
        return "file:" + (Objects.requireNonNull(CardAssetsProvider.class.getResource(
                hiddenCardPath + "hiddenCard.png"))).getFile();
    }
}