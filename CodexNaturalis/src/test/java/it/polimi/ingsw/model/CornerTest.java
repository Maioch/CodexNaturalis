package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class CornerTest {
    /**

    private Corner c1 = new Corner(1,1,Content.RED);
    private Corner c2 = new Corner(-1,0,Content.WHITE);
    private Corner c3 = new Corner(1,1,Content.WHITE);
    private Corner c4 = new Corner(-1,0,Content.BLUE);
    private Corner bc1 = new Corner(2,-1);

    @Test
    void getX(){
        assertEquals(1, c1.getX());
        assertEquals(-1, c2.getX());
        assertEquals(2, bc1.getX());
    }

    @Test
    void getY(){
        assertEquals(1, c1.getY());
        assertEquals(0, c2.getY());
        assertEquals(-1, bc1.getY());
    }

    @Test
    void getContent(){
        assertEquals(Content.RED, c1.getContent());
        assertEquals(Content.EMPTY, c2.getContent());
        assertNull(bc1.getContent());
    }

    @Test
    void getVisibility(){
        assertTrue(c1.getVisibility());
        assertTrue(c2.getVisibility());
        assertFalse(bc1.getVisibility());
    }

    @Test
    void coverCorner1(){
        c1.coverCorner();
        assertFalse(c1.getVisibility());
    }

    @Test
    void coverCorner2(){
        bc1.coverCorner();
        assertFalse(bc1.getVisibility());
    }

    @Test
    void isSamePosition(){
        assertTrue(c1.isSamePosition(c3));
        assertFalse(c2.isSamePosition(c1));
    }

    */
}
