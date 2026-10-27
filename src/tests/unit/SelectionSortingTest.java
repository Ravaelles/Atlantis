package tests.unit;

import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
@Tag("infra")
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

    @Test
    public void nearestToReturnsClosestAndNulls() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            assertSame(a, Select.our().nearestTo(APosition.create(10, 10)));
            assertSame(null, Select.our().nearestTo(null));
            assertSame(null, Select.our().ofType(AUnitType.Protoss_Carrier).nearestTo(APosition.create(10, 10)));
        });
    }

    @Test
    public void groundNearestAndFarthestDisagree() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            assertSame(a, Select.our().groundNearestTo(APosition.create(10, 10)));
            assertSame(c, Select.our().groundFarthestTo(APosition.create(10, 10)));
            assertSame(c, Select.our().mostDistantTo(APosition.create(10, 10)));
        });
    }

    @Test
    public void nearestToOrNullRespectsMaxLength() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            assertSame(a, Select.our().nearestToOrNull(APosition.create(10, 10), 0.5));
            assertSame(null, Select.our().nearestToOrNull(APosition.create(50, 50), 0.5));
        });
    }

    @Test
    public void mostWoundedPrefersWoundedOtherwiseNearest() {
        FakeUnit healthyNear = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit healthyFar = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit hurt = fake(AUnitType.Terran_Marine, 20, 20).setHp(10);

        world(1, fakeOurs(healthyNear, healthyFar, hurt), fakeEnemies(), () -> {
            assertSame(hurt, Select.our().mostWounded());
            assertSame(hurt, Select.our().mostWoundedOrNearest(APosition.create(10, 10)));
            assertSame(hurt, Select.our().lowestHealth());
        });
    }

    @Test
    public void unwoundedFallsBackToNearest() {
        FakeUnit h1 = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit h2 = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(h1, h2), fakeEnemies(), () -> {
            assertSame(h2, Select.our().mostWoundedOrNearest(APosition.create(12, 10)));
        });
    }
}
