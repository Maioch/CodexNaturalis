package it.polimi.ingsw.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class BasicCardTest {
    private final HashMap<Location, Corner> corners1 = new HashMap<Location,Corner>(){{
        put(Location.TR, new Corner(0,1,Content.RED));
        put(Location.TL, new Corner(1,1,Content.RED));
        put(Location.BR, new Corner(0,0,Content.RED));
        put(Location.BL, new Corner(1,0,Content.RED));
    }
    };

    private final ArrayList<Content> content1 = new ArrayList<Content>(Arrays.asList(Content.RED, Content.BLUE));
    private final BasicCard card1 = new BasicCard(0, Content.RED, corners1, 0, content1);

    @Test
    void getPoints(){
        assertEquals(0, card1.getPoints());
    }
}
