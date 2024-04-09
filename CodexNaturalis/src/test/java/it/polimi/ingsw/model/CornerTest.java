package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CornerTest {
    private final Corner c0 = new Corner(Content.RED);
    private final Corner c1 = new Corner(Content.PURPLE);

    @Test
    void isSamePosition(){
        assertTrue(c0.isSamePosition(c1));
    }

    @Test
    void getX(){
        assertEquals(0, c0.getX());
        c1.setX(1);
        assertEquals(1, c1.getX());
    }

    @Test
    void getY(){
        assertEquals(0, c0.getY());
        c1.setY(-1);
        assertEquals(-1, c1.getY());
    }

    @Test
    void getContent(){
        assertEquals(Content.RED, c0.getContent());
        assertEquals(Content.PURPLE, c1.getContent());
    }

    @Test
    void visibility(){
        assertTrue(c0.getVisibility());
        c1.coverCorner();
        assertFalse(c1.getVisibility());
    }

    @Test
    void equals(){
        assertNotEquals(c0, c1);
        c1.coverCorner();
        c0.coverCorner();
        assertEquals(c0, c1);
        c1.setX(3);
        assertNotEquals(c0, c1);
    }
}