package tests.unit;

import atlantis.map.choke.AChoke;
import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionsTest extends AbstractTestWithUnits {
    @Test
    public void average() {
        Positions<APosition> positions = new Positions<>();
        APosition p1 = APosition.create(1, 19);
        APosition p2 = APosition.createFromPixels(19 * 32 + 8, 32);
        APosition p3 = APosition.create(2, 3);
        APosition p4 = APosition.create(4, 5);
        positions.addPosition(p1);
        positions.addPosition(p2);

        assertEquals(2, positions.size());
        assertEquals(APosition.createFromPixels(10 * 32 + 4, 10 * 32), positions.average());
        assertEquals(p1, positions.first());
        assertEquals(p2, positions.get(1));
        assertEquals(p2, positions.sortByDistanceTo(p2, true).first());
        assertEquals(p1, positions.sortByDistanceTo(p2, false).first());
        assertEquals(1, positions.limit(1).size());

        positions = positions.addPosition(p3).addPosition(p4);

        assertEquals(4, positions.size());

        positions = positions.limit(3);

        assertEquals(3, positions.size());

        positions = positions.removePosition(p1);

        assertEquals(2, positions.size());
        assertEquals(p2, positions.get(0));
    }

    @Test
    public void sortByDistance() {
        Positions<APosition> positions = new Positions<>();
        APosition p1 = APosition.create(1, 19);
        APosition p2 = APosition.create(2, 3);
        APosition p3 = APosition.create(3, 4);

        positions.addPosition(p1);
        positions.addPosition(p2);
        positions.addPosition(p3);

        positions = positions.sortByDistanceTo(p3, true);
//        positions.print();

        assertEquals(p3, positions.get(0));
        assertEquals(p1, positions.get(positions.size() - 1));

        positions = positions.sortByDistanceTo(p3, false);
//        positions.print();

        assertEquals(p1, positions.get(0));
        assertEquals(p3, positions.get(positions.size() - 1));
    }

    @Test
    public void sortByGroundDistance() {
        Positions<APosition> positions = new Positions<>();
        APosition p1 = APosition.create(1, 19);
        APosition p2 = APosition.create(2, 3);
        APosition p3 = APosition.create(3, 4);

        positions.addPosition(p1);
        positions.addPosition(p2);
        positions.addPosition(p3);

        positions = positions.sortByGroundDistanceTo(p3, true);

        assertEquals(p3, positions.get(0));
        assertEquals(p1, positions.get(positions.size() - 1));

        positions = positions.sortByGroundDistanceTo(p3, false);

        assertEquals(p1, positions.get(0));
        assertEquals(p3, positions.get(positions.size() - 1));
    }
}
