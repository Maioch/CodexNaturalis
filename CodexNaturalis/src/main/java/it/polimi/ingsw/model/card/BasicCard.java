package it.polimi.ingsw.model.card;

import it.polimi.ingsw.model.Content;
import it.polimi.ingsw.model.Corner;
import it.polimi.ingsw.model.Location;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A class that represents a "basic" card, which includes resource cards and starter cards.
 * It has a unique ID, and has a specific color. There can be a maximum of 4 corners.
 * @author Francesco Saverio Nisoli
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

        int totDistance;
        boolean coherentCoordinates;

        //that portion of the code is needed, in order to verify that corner's coordinates are coherent (Xs and Ys must have a certain distance between corners)
        coherentCoordinates = true;
        if(corners.get((Location.BL)).getX() != corners.get((Location.TL)).getX() ||
                corners.get((Location.BR)).getX() != corners.get((Location.TR)).getX() ||
                corners.get((Location.BR)).getY() != corners.get((Location.BL)).getY() ||
                corners.get((Location.TR)).getY() != corners.get((Location.TL)).getY()
        ){
           coherentCoordinates = false;
        }

        totDistance = (corners.get((Location.BR)).getX() - corners.get(Location.BL).getX()) +
                (corners.get((Location.TR)).getX() - corners.get(Location.TL).getX()) +
                (corners.get((Location.TL)).getY() - corners.get(Location.BL).getY()) +
                (corners.get((Location.TR)).getY() - corners.get(Location.BR).getY());

        if(!color.isColor() || points<0 || totDistance == 4 || !coherentCoordinates){
            throw new RuntimeException();
        }

        this.cardId = cardId;
        this.color = color;
        this.corners = corners;
        this.points = points;
        this.resources = resources;
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
        return resources;
    }
}
