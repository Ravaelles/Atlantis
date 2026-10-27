package tests.unit;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pinning the distance-answer family before its extraction out of
 * {@code Selection} (NEXT #10): nearest distance, with the 9999
 * empty-selection sentinel intact.
 */
@Tag("infra")
public class SelectionDistancesTest extends WorldStubForTests {

    @Test
    public void distancesReadOffTheNearest() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 10, 10);
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 14, 10);

        world(1, fakeOurs(zealot, dragoon), fakeEnemies(), () -> {
            Selection ours = Select.our();
            APosition probe = APosition.create(20, 10);

            assertEquals(6.0, ours.distToNearest(probe), 0.001);
            assertEquals(6.0, ours.nearestToDist(probe), 0.001);
            assertTrue(ours.nearestToDistLess(probe, 7));
            assertFalse(ours.nearestToDistLess(probe, 5));
        });
    }

    @Test
    public void emptySelectionReportsFarAway() {
        world(1, fakeOurs(), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(9999.0, ours.distToNearest(APosition.create(20, 10)), 0.001);
            assertEquals(9999.0, ours.nearestToDist(APosition.create(20, 10)), 0.001);
            assertFalse(ours.nearestToDistLess(APosition.create(20, 10), 100000));
        });
    }
}
