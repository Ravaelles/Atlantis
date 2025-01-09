package tests.unit;

import atlantis.map.position.APosition;
import atlantis.map.position.Positions;
import atlantis.units.AUnitType;
import net.bytebuddy.dynamic.Nexus;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PositionUtilTest extends AbstractTestWithUnits {
    @Test
    public void distToUnit() {
        FakeUnit unit = new FakeUnit(AUnitType.Protoss_Nexus, 10, 10);
        APosition position = APosition.create(13, 10);

        // This will not really work for FakeUnit as there's no real Unit.
        // It needs to have a copy of Unit->getDistance(Unit target)
        assertEquals(3.0, position.distTo(unit));
        assertEquals(3.0, unit.distTo(position));
        assertEquals(3.0, unit.position().distTo(position));
    }
}
