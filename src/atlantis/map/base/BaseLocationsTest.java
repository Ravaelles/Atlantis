package atlantis.map.base;

import atlantis.map.position.APosition;
import org.junit.Test;
import tests.acceptance.NonAbstractTestFakingGame;

import static org.junit.Assert.assertEquals;

public class BaseLocationsTest extends NonAbstractTestFakingGame {
    @Test
    public void testNearestTo() {
        ABaseLocation base1 = BaseLocations.nearestTo(APosition.create(9, 46));
        ABaseLocation base2 = BaseLocations.nearestTo(APosition.create(9, 2));

        assertEquals(7, base1.tx());
        assertEquals(44, base1.ty());
        assertEquals(14, base2.tx());
        assertEquals(13, base2.ty());
    }
}
