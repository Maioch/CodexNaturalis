package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ContentTest {
    private final Content c1 = Content.RED;
    private final Content c2 = Content.GREEN;
    private final Content c3 = Content.BLUE;
    private final Content c4 = Content.PURPLE;
    private final Content c5 = Content.PEN;
    private final Content c6 = Content.PAPER;
    private final Content c7 = Content.INK;
    private final Content c8 = Content.EMPTY;
    private final Content c9 = Content.WHITE;

    /**
     * Tests the isColor method, by checking every possible content value
     */
    @Test
    void isColorTest(){
        assertTrue(c1.isColor());
        assertTrue(c2.isColor());
        assertTrue(c3.isColor());
        assertTrue(c4.isColor());
        assertTrue(c9.isColor());
        assertFalse(c5.isColor());
        assertFalse(c6.isColor());
        assertFalse(c7.isColor());
        assertFalse(c8.isColor());
    }

    /**
     * Tests the isObject method, by checking every possible content value
     */
    @Test
    void isObjectTest(){
        assertFalse(c1.isObject());
        assertFalse(c2.isObject());
        assertFalse(c3.isObject());
        assertFalse(c4.isObject());
        assertFalse(c8.isObject());
        assertFalse(c9.isObject());
        assertTrue(c5.isObject());
        assertTrue(c6.isObject());
        assertTrue(c7.isObject());
    }

    /**
     * Tests the isEmpty method, by checking every possible content value
     */
    @Test
    void isEmptyTest(){
        assertFalse(c1.isEmpty());
        assertFalse(c2.isEmpty());
        assertFalse(c3.isEmpty());
        assertFalse(c4.isEmpty());
        assertFalse(c9.isEmpty());
        assertFalse(c5.isEmpty());
        assertFalse(c6.isEmpty());
        assertFalse(c7.isEmpty());
        assertTrue(c8.isEmpty());
    }
}
