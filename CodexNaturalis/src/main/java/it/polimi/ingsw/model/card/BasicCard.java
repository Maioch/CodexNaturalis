package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;

import javax.swing.text.AbstractDocument;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A class that represents a "basic" card, which includes resource cards and starter cards.
 * It has a unique ID, and has a specific color. There can be a maximum of 4 corners.
 * The resources attribute refers only to the resources found in the center of the card, not in the corners.
 * @author Francesco Saverio Nisoli, Guglielmo Gatti
 */
public class BasicCard {

    protected final int cardId;
    protected final Content color;
    protected final HashMap<Location, Corner> corners;
    protected final int points;
    protected final ArrayList<Content> resources;

    /**
     * Constructor of the BasicCard
     * @param cardId the card's id
     * @param color the "color" of the card.
     * @param corners the card's corners, represented by
     * @param points the card's points, value which represent the sore's incrementation when the card is played
     * @param resources the card's resources, an array of the contained resources
     */

    BasicCard(int cardId, Content color, HashMap<Location, Corner> corners, int points, ArrayList<Content> resources) throws RuntimeException{

        if(!color.isColor() ||
                points<0 ||
                checkIfCoherentCorners(true, corners) ||
                checkIfCoherentCorners(false, corners) ||
                whiteInCorners(corners)
        )
        {
            throw new RuntimeException();
        }

        this.cardId = cardId;
        this.color = color;
        this.corners = (HashMap<Location, Corner>) corners.clone();
        this.points = points;
        this.resources = (ArrayList<Content>) resources.clone();
    }

    /**
     * Getter of the "points" attribute
     * @return the point attribute
     */
    public int getPoints(){
        return points;
    }

    /**
     * Getter of the "color" attribute
     * @return the card's color
     */
    public Content getColor(){
        return color;
    }

    /**
     * Getter of the "resources" attribute list
     * @return the card's resources
     */
    public ArrayList<Content> getResources() {
        return (ArrayList<Content>) resources.clone();
    }

    /**
     * Returns a hashmap that associates each resource type with the amount present in the card by pulling
     * from both the corners and the permanent resources
     * @return an hashmap with the resource as key and the amount as value
     */
    public HashMap<Content,Integer> getCardSymbols(){
        //Create a list containing all the contents of the card
        ArrayList<Content> totalContent = corners.values().stream()
                .map(Corner::getContent)
                .toList();
        totalContent.addAll(resources);
        return new HashMap<Content,Integer>(){{
            for(Content content : Content.values()){
                put(content, totalContent.stream()
                        .filter(x -> x == content)
                        .mapToInt(x -> 1)
                        .reduce(0,Integer::sum));
            }
        }};
    }

    public ArrayList<Corner> getValidCorners(){
        return corners.values().stream().filter(Corner::getVisibility).toList();
    }

    /**
     * A private method that checks the corner's coherence, which is based on the coordinates distances.
     * To have a complete check, it's necessary to control (and so run this method) on both the axes.
     * @param isXAxis selects the axis on which the control is based ("0" represents the x-axis, "1" represents the y-axis)
     * @param corners HashMap containing the corners checked by the method
     * @return True if the corners are coherent, false if they're not.
     */
    private boolean checkIfCoherentCorners(boolean isXAxis, HashMap<Location, Corner> corners){

        if(isXAxis){
            if(corners.get((Location.BL)).getX() != corners.get((Location.TL)).getX() ||
                    corners.get((Location.BR)).getX() != corners.get((Location.TR)).getX()){
                return false;
            }else if((corners.get((Location.BR)).getX() - corners.get(Location.BL).getX()) != 1 ||
                    (corners.get((Location.TR)).getX() - corners.get(Location.TL).getX()) != 1){
                return false;
            }
        }else{
            if(corners.get((Location.BR)).getY() != corners.get((Location.BL)).getY() ||
                    corners.get((Location.TR)).getY() != corners.get((Location.TL)).getY()){
                return false;
            } else if ((corners.get((Location.TL)).getY() - corners.get(Location.BL).getY()) != 1 ||
                    (corners.get((Location.TR)).getY() - corners.get(Location.BR).getY()) != 1) {
                return false;
            }
        }

        return true;
    }

    /**
     * A method that searches if there's any corner containing a "WHITE" content value
     * @param corners HashMap containing the corners checked by the method
     * @return True if there's a corner with "WHITE" content, false otherwise
     */
    private boolean whiteInCorners(HashMap<Location, Corner> corners){
        return corners.values().stream().filter(x -> x.getContent() == Content.WHITE).toList().isEmpty();
    }

}
