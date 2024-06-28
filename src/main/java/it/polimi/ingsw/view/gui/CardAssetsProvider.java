package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.core.Parameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides the assets for the cards to the GUI.
 *
 * @author Andrea Fidanza, Marco Maiocchi, Francesco Nisoli, Guglielmo Gatti
 */
public class CardAssetsProvider {

    //the path where the assets for the fronts of the cards are found.
    private static final String frontPath = "/scenes/images/cardFronts/";

    //the path where the assets for the backs of the cards are found.
    private static final String backPath = "/scenes/images/cardBacks/";

    //the path where the assets for the objective cards are found.
    private static final String objectivesPath = "/scenes/images/cardFronts/";

    //the path where the assets for the hidden cards are found.
    private static final String hiddenCardPath = "/scenes/images/interface/";

    private static final Map<Content, String> resourcesBacks = new HashMap<>(){{
        for(Content content : Arrays.stream(Content.values()).filter(Content::isResource).toList()){
            put(content, backPath + content + "resource.png");
        }
    }};
    private static final Map<Content, String> goldsBacks = new HashMap<>(){{
        for(Content content : Arrays.stream(Content.values()).filter(Content::isResource).toList()){
            put(content, backPath + content + "gold.png");
        }
    }};

    /**
     * Class constructor.
     */
    public CardAssetsProvider() {}

    /**
     * Returns the path where the parameter card is saved.
     * Resource and gold cards have the same back side, except for the color: if the card isn't a starter and its back
     * side is requested, the returned ID is the same for every card with the same color.
     *
     * @param card the card requested.
     *
     * @return     the path the card is saved in.
     *
     * @see BasicCard
     */
    public static String getCardFilePath(BasicCard card){
        int id = card.getCardId();
        boolean isFront = card.isFront();
        int startIndex = Parameters.getStartCardIndex(CardType.STARTER);
        int endIndex = Parameters.getEndCardIndex(CardType.STARTER);
        if(!isFront && (id < startIndex || id > endIndex)){
            boolean isResource = id >= Parameters.getStartCardIndex(CardType.RESOURCE) &&
                    id <= Parameters.getEndCardIndex(CardType.RESOURCE);
            return isResource ? resourcesBacks.get(card.getColor()) : goldsBacks.get(card.getColor());
        }
        return (isFront ? frontPath : backPath) + id + ".png";
    }

    /**
     * Returns the path where the parameter objective is saved.
     *
     * @param objective the objective requested.
     *
     * @return          the path the objective is saved in.
     *
     * @see Objective
     */
    public static String getObjectiveFilePath(Objective objective){
        return objectivesPath + objective.getObjectiveId() + ".png";
    }

    /**
     * Gets the hidden card image path.
     * The hidden card is a card that has the same pattern of the game table in it.
     *
     * @return the path the hidden card is saved in.
     */
    public static String getHiddenCardFilePath(){
        return hiddenCardPath + "hiddenCard.png";
    }
}