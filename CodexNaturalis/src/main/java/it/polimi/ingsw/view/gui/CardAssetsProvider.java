package it.polimi.ingsw.view.gui;

import it.polimi.ingsw.model.shared.Content;
import it.polimi.ingsw.model.server.GameParameters;
import it.polimi.ingsw.model.shared.card.BasicCard;
import it.polimi.ingsw.model.shared.card.CardType;
import it.polimi.ingsw.model.shared.card.Objective;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * CardAssetsProvider
 */
public class CardAssetsProvider {

    private static final String frontPath = "/scenes/images/cardFronts/";
    private static final String backPath = "/scenes/images/cardBacks/";
    private static final String objectivesPath = "/scenes/images/cardFronts/";
    private static final String hiddenCardPath = "/scenes/images/";
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
            boolean isResource = id >= GameParameters.getStartCardIndex(CardType.RESOURCE) &&
                    id <= GameParameters.getEndCardIndex(CardType.RESOURCE);
            return isResource ? resourcesBacks.get(card.getColor()) : goldsBacks.get(card.getColor());
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