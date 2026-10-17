package tests.unit;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pinning the ordering-family semantics before its extraction out of
 * {@code Selection} (NEXT #10, slice 3): distance sorts mutate the
 * selection data in place, farthest-first is the exact reverse, null and
 * singleton inputs are no-ops, health/wound sorts put the most damaged
 * unit first.
 *
 * <p>Written against the current implementation; the extraction that
 * follows must keep every assertion green without touching them.</p>
 */
public class SelectionSortingTest extends WorldStubForTests {

    @Test
    public void distanceSortNearestFirstAndFarthestFirst() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            List<atlantis.units.AUnit> nearestFirst =
                Select.our().sortDataByDistanceTo(APosition.create(10, 10), true);

            assertEquals(3, nearestFirst.size());
            assertSame(a, nearestFirst.get(0));
            assertSame(b, nearestFirst.get(1));
            assertSame(c, nearestFirst.get(2));

            List<atlantis.units.AUnit> farthestFirst =
                Select.our().sortDataByDistanceTo(APosition.create(10, 10), false);

            assertSame(c, farthestFirst.get(0));
            assertSame(b, farthestFirst.get(1));
            assertSame(a, farthestFirst.get(2));
        });
    }

    @Test
    public void singletonAndNullPositionAreNoops() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            Selection single = Select.our().ofType(AUnitType.Protoss_Probe).limit(1);
            assertEquals(1, single.sortDataByDistanceTo(APosition.create(10, 10), true).size());

            List<atlantis.units.AUnit> untouched =
                Select.our().sortDataByDistanceTo((atlantis.map.position.HasPosition) null, true);

            assertEquals(2, untouched.size());
            assertSame(a, untouched.get(0));
            assertSame(b, untouched.get(1));
        });
    }

    @Test
    public void groundSortOrdersNearestFirst() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            List<atlantis.units.AUnit> ordered =
                Select.our().sortDataByGroundDistanceTo(APosition.create(10, 10), true);

            assertSame(a, ordered.get(0));
            assertSame(b, ordered.get(1));
            assertSame(c, ordered.get(2));
        });
    }

    @Test
    public void sortByNearestToOrdersTheSelection() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ordered = Select.our().sortByNearestTo(c);

            assertEquals(3, ordered.size());
            assertSame(c, ordered.first());
            assertTrue(ordered.last().equals(a) || ordered.last().equals(b));

            Selection empty = Select.our().ofType(AUnitType.Protoss_Carrier);
            assertEquals(0, empty.sortByNearestTo(c).size());
        });
    }

    @Test
    public void healthAndWoundSortsPutMostDamagedFirst() {
        FakeUnit healthy = fake(AUnitType.Terran_Marine, 10.3).setHp(40);
        FakeUnit hurt = fake(AUnitType.Terran_Marine, 12.1).setHp(10);

        world(1, fakeOurs(healthy, hurt), fakeEnemies(), () -> {
            assertSame(hurt, Select.our().sortByHealth().first());
            assertSame(hurt, Select.our().sortByWound().first());
        });
    }
}
