package it.polimi.ingsw.model.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void getSymbolTest(){
        assertEquals("\u001B[41m  \u001B[m", c1.getSymbol());
        assertEquals("\u001b[42m  \u001b[m", c2.getSymbol());
        assertEquals("\u001b[44m  \u001b[m", c3.getSymbol());
        assertEquals("\u001b[45m  \u001b[m", c4.getSymbol());
        assertEquals("^^", c5.getSymbol());
        assertEquals("[]", c6.getSymbol());
        assertEquals("()", c7.getSymbol());
        assertEquals("\u001b[m  \u001b[m", c8.getSymbol());
        assertEquals("\u001b[47;1m  \u001b[m", c9.getSymbol());
    }

    @Test
    void getTextColorStringTest(){
        assertEquals("\u001B[31m", c1.getTextColorString());
        assertEquals("\u001B[32m", c2.getTextColorString());
        assertEquals("\u001B[34m", c3.getTextColorString());
        assertEquals("\u001B[35m", c4.getTextColorString());
        assertEquals("\u001B[0m", c5.getTextColorString());
        assertEquals("\u001B[0m", c6.getTextColorString());
        assertEquals("\u001B[0m", c7.getTextColorString());
        assertEquals("\u001B[0m", c8.getTextColorString());
        assertEquals("\u001B[0m", c9.getTextColorString());
    }

    @Test
    void getHexColorStringTest(){
        assertEquals("#f14624", c1.getHexColorString());
        assertEquals("#2d853a", c2.getHexColorString());
        assertEquals("#5cc7b1", c3.getHexColorString());
        assertEquals("#8d1a85", c4.getHexColorString());
        assertEquals("#FFFFFF", c5.getHexColorString());
        assertEquals("#FFFFFF", c6.getHexColorString());
        assertEquals("#FFFFFF", c7.getHexColorString());
        assertEquals("#FFFFFF", c8.getHexColorString());
        assertEquals("#FFFFFF", c9.getHexColorString());
    }
}
