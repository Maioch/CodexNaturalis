package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

public class CornerTest {
    @Test
    void isSamePosition(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertTrue(c0.isSamePosition(c1));
    }

    @Test
    void getX(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(0, c0.getX());
        c1.setX(1);
        assertEquals(1, c1.getX());
    }

    @Test
    void getY(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(0, c0.getY());
        c1.setY(-1);
        assertEquals(-1, c1.getY());
    }

    @Test
    void getContent(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertEquals(Content.RED, c0.getContent());
        assertEquals(Content.PURPLE, c1.getContent());
    }

    @Test
    void visibility(){
        Corner c0 = new Corner(Content.RED, Location.TR);
        Corner c1 = new Corner(Content.PURPLE, Location.TR);
        assertTrue(c0.getVisibility());
        c1.coverCorner();
        assertFalse(c1.getVisibility());
    }

    @Test
    void equals(){
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