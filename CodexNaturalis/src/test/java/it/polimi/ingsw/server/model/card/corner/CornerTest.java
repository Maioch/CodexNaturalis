package it.polimi.ingsw.server.model.card.corner;

import static org.junit.jupiter.api.Assertions.*;

import it.polimi.ingsw.model.server.Content;
import it.polimi.ingsw.model.server.card.corner.Corner;
import it.polimi.ingsw.model.server.card.corner.Location;
import org.junit.jupiter.api.Test;

public class CornerTest {
    @Test
    void isSamePositionTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertTrue(c0.isSamePosition(c1));
    }

    @Test
    void getXTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(0, c0.getX());
        c1.setX(1);
        assertEquals(1, c1.getX());
    }

    @Test
    void getYTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(0, c0.getY());
        c1.setY(-1);
        assertEquals(-1, c1.getY());
    }

    @Test
    void getContentTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(Content.RED, c0.getContent());
        assertEquals(Content.PURPLE, c1.getContent());
    }

    @Test
    void visibilityTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertTrue(c0.getVisibility());
        c1.coverCorner();
        assertFalse(c1.getVisibility());
    }

    @Test
    void equalsTest(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.RED, Location.TR);
        c1.coverCorner();
        assertNotEquals(c0, c1);
        c0.coverCorner();
        assertEquals(c0, c1);
        c1.setX(3);
        assertNotEquals(c0, c1);
    }
}