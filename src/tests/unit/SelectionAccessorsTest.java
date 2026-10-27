package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pinning the element-accessor semantics before their extraction out of
 * {@code Selection} (NEXT #10, slice 5): positional access answers by
 * list order, empty selections answer null (never an exception), a fixed
 * seed is deterministic and stays inside the set.
 *
 * <p>Written against the current implementation; the extraction that
 * follows must keep every assertion green without touching them.</p>
 */
@Tag("infra")
public class SelectionAccessorsTest extends WorldStubForTests {

    @Test
    public void firstSecondLastFollowListOrder() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertSame(a, ours.first());
            assertSame(b, ours.second());
            assertSame(c, ours.last());
        });
    }

    @Test
    public void busyAndIdAnswers() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertFalse(ours.areAllBusy(), "idle probes are not all busy");
            a.busy = true;
            assertFalse(ours.areAllBusy(), "one idle probe vetoes");
            b.busy = true;
            assertTrue(ours.areAllBusy(), "all busy means all busy");

            String ids = ours.unitIds();
            assertTrue(ids.startsWith("ids(") && ids.endsWith(")"), "cache-path shape: " + ids);
            assertTrue(ids.contains(String.valueOf(a.id())), "names a: " + ids);
            assertTrue(ids.contains(String.valueOf(b.id())), "names b: " + ids);
        });
    }

    @Test
    public void emptySelectionAnswersNull() {
        world(1, fakeOurs(), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(0, ours.count());
            assertNull(ours.first(), "no first of nothing");
            assertNull(ours.second(), "no second of nothing");
            assertNull(ours.last(), "no last of nothing");
            assertNull(ours.random(), "no random of nothing");
            assertTrue(ours.isEmpty());
        });
    }

    @Test
    public void seededRandomIsDeterministicAndInsideTheSet() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Probe, 12, 10);
        FakeUnit c = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b, c), fakeEnemies(), () -> {
            Selection ours = Select.our();

            assertEquals(
                ours.randomWithSeed(7), ours.randomWithSeed(7),
                "same seed, same answer");
            assertTrue(
                ours.randomWithSeed(7) == a
                    || ours.randomWithSeed(7) == b
                    || ours.randomWithSeed(7) == c,
                "a seeded pick stays inside the selection");
            atlantis.units.AUnit pick = ours.random();
            assertTrue(
                pick == a || pick == b || pick == c,
                "an unseeded pick stays inside the selection: " + pick);
        });
    }
}
