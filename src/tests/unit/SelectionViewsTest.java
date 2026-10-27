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
 * Pinning the container-view semantics before their extraction out of
 * {@code Selection} (NEXT #10, slice 6): list/units materialize the same
 * elements, count/size/predicates always agree, empty selections answer
 * zeros and falses (never an exception).
 *
 * <p>Written against the current implementation; the extraction that
 * follows must keep every assertion green without touching them.</p>
 */
@Tag("infra")
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

    @Test
    public void addAppendsUnitsFromSelectionCollectionAndSingle() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);
        FakeUnit c = fake(AUnitType.Protoss_Dragoon, 30, 30);

        world(1, fakeOurs(a), fakeEnemies(), () -> {
            Selection grown = Select.our().add(Select.our().cloneByRemovingIf(u -> u != a, "only-a"));
            assertTrue(grown.list().contains(a));

            Selection fromCollection = Select.our().add(java.util.Collections.singletonList(b));
            assertTrue(fromCollection.list().contains(b));

            Selection single = Select.our().add(c);
            assertTrue(single.list().contains(c));
            assertEquals(2, single.count());
        });
    }

    @Test
    public void excludeRemovesSingleCollectionAndSelection() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            assertEquals(1, Select.our().exclude(a).count());
            assertFalse(Select.our().exclude(a).list().contains(a));

            assertEquals(0, Select.our().exclude(java.util.Arrays.asList(a, b)).count());

            Selection other = Select.our().cloneByRemovingIf(u -> u != b, "only-b");
            Selection rest = Select.our().exclude(other);
            assertEquals(1, rest.count());
            assertTrue(rest.list().contains(a));

            assertEquals(2, Select.our().exclude((FakeUnit) null).count());
        });
    }

    @Test
    public void reverseFlipsOrderInPlace() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            Selection ours = Select.our();
            assertEquals(a, ours.list().get(0));

            assertSame(ours.reverse(), ours);
            assertEquals(b, ours.list().get(0));
            assertEquals(a, ours.list().get(1));
        });
    }

    @Test
    public void removeDuplicatesLimitAndCountOfType() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            Selection doubled = Select.our().add(a);
            assertEquals(3, doubled.count());
            assertEquals(2, doubled.removeDuplicates().count());

            assertEquals(1, Select.our().limit(1).count());
            assertEquals(2, Select.our().limit(99).count());

            assertEquals(1, Select.our().countOfType(AUnitType.Protoss_Probe));
            assertEquals(1, Select.our().countOfType(AUnitType.Protoss_Zealot));
            assertEquals(0, Select.our().countOfType(AUnitType.Protoss_Dragoon));
            assertEquals(2, Select.our().countOfType(AUnitType.Protoss_Probe, AUnitType.Protoss_Zealot));
        });
    }

    @Test
    public void aggregatesSumHpHitsAndShields() {
        FakeUnit a = fake(AUnitType.Protoss_Probe, 10, 10);
        FakeUnit b = fake(AUnitType.Protoss_Zealot, 20, 20);
        a.setHp(20);
        a.setShields(0);
        b.setHp(80);
        b.setShields(60);

        world(1, fakeOurs(a, b), fakeEnemies(), () -> {
            assertEquals(100, Select.our().totalHp());
            assertEquals(30, Select.our().averageShields());
            assertTrue(Select.our().totalHits() >= 0);
            assertEquals(0, Select.our().cloneByRemovingIf(u -> true, "empty").totalHp());
        });
    }
}
