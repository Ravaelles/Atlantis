package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Pinning the container-view semantics before their extraction out of
 * {@code Selection} (NEXT #10, slice 6): list/units materialize the same
 * elements, count/size/predicates always agree, empty selections answer
 * zeros and falses (never an exception).
 *
 * <p>Written against the current implementation; the extraction that
 * follows must keep every assertion green without touching them.</p>
 */
public class SelectionViewsTest extends WorldStubForTests {

    @Test
    public void listAndUnitsMaterializeTheSameElements() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(3, ours.list().size());
            assertTrue(ours.list().contains(a));
            assertTrue(ours.list().contains(b));
            assertTrue(ours.list().contains(c));
            assertEquals(3, ours.units().size());
        });
    }

    @Test
    public void countSizeAndPredicatesAgree() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(2, ours.count());
            assertEquals(2, ours.size());
            assertFalse(ours.isEmpty());
            assertFalse(ours.empty());
            assertTrue(ours.isNotEmpty());
            assertTrue(ours.notEmpty());
            assertTrue(ours.anyExists());
            assertTrue(ours.atLeast(2));
            assertFalse(ours.atLeast(3));
            assertTrue(ours.atMost(2));
            assertFalse(ours.atMost(1));
        });
    }

    @Test
    public void emptySelectionAnswersZerosAndFalses() {
        world(1, fakeOurs(), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(0, ours.count());
            assertEquals(0, ours.size());
            assertEquals(0, ours.list().size());
            assertEquals(0, ours.units().size());
            assertTrue(ours.isEmpty());
            assertTrue(ours.empty());
            assertFalse(ours.isNotEmpty());
            assertFalse(ours.notEmpty());
            assertFalse(ours.anyExists());
            assertFalse(ours.atLeast(1));
            assertTrue(ours.atMost(0));
        });
    }
}
