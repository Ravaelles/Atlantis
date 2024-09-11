package tests.unit;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnitType;
import org.junit.Test;

import static junit.framework.TestCase.assertEquals;

public class PositionsTest extends AbstractTestWithUnits {
    @Test
    public void average() {
        Positions<APosition> positions = new Positions<>();
        APosition p1 = APosition.create(1, 19);
        APosition p2 = APosition.createFromPixels(19 * 32 + 8, 32);
        positions.addPosition(p1);
        positions.addPosition(p2);

//        APosition expected = APosition.createFromPixels(10 * 32 + 4, 10 * 32);
//        APosition actual = positions.average();

//        System.err.println("expected = " + expected.toStringPixels());
//        System.err.println("actual = " + actual.toStringPixels());

        assertEquals(APosition.createFromPixels(10 * 32 + 4, 10 * 32), positions.average());
        assertEquals(p1, positions.first());
        assertEquals(p2, positions.get(1));
        assertEquals(p2, positions.sortByDistanceTo(p2, true).first());
        assertEquals(p1, positions.sortByDistanceTo(p2, false).first());
    }
}
