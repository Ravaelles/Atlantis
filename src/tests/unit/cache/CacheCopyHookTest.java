package tests.unit.cache;

import atlantis.units.select.Select;
import atlantis.units.select.Selection;
import atlantis.util.cache.Cache;
import atlantis.util.cache.Copyable;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static atlantis.units.AUnitType.Terran_Marine;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * {@link Cache#get(String, int, atlantis.util.Callback)} hands a cached value out as
 * a copy when it says it is copyable, because two of the mutations a caller can make
 * on a selection rewrite it in place: {@code sortByNearestTo} and {@code sortByHealth}
 * call {@code data.sort(...)} and {@code return this}.
 *
 * <p>Before the copy hook became {@code atlantis.util.cache.Copyable}, the cache asked
 * the question about {@code Selection} by name ({@code instanceof} + {@code clone()}),
 * which is what the two frozen util-rule entries were. This test exists so that
 * renaming the hook to an interface cannot quietly drop the copy: nothing else in the
 * suite asserts it.</p>
 */
public class CacheCopyHookTest extends WorldStubForTests {

    @Test
    public void sortingWhatWasHandedOutDoesNotReorderTheNextCaller() {
        FakeUnit left = fake(Terran_Marine, 10);
        FakeUnit right = fake(Terran_Marine, 50);

        world(1, fakeOurs(left, right), fakeEnemies(), () -> {
            Cache<Selection> cache = new Cache<>();

            Selection first = cache.get("sel", 100, () -> Select.our());
            Selection second = cache.get("sel", 100, () -> fail("cache hit expected, not a recompute"));

            assertNotSame(first, second, "a copyable value must be handed out as a copy");
            assertEquals(2, second.size());

            Selection notBefore = cache.get("sel", 100, () -> fail("cache hit expected, not a recompute"));
            assertEquals(2, notBefore.size());
            assertEquals(left.id(), notBefore.list().get(0).id(), "the cached order is the computed one");

            first.sortByNearestTo(right);
            assertEquals(right.id(), first.list().get(0).id(), "the caller's own copy is sorted in place");

            Selection afterTheSort = cache.get("sel", 100, () -> fail("cache hit expected, not a recompute"));
            assertEquals(left.id(), afterTheSort.list().get(0).id(),
                "an in-place sort by one caller must not reorder the cached selection for the next one");
            assertEquals(right.id(), afterTheSort.list().get(1).id());
        });
    }

    @Test
    public void aValueThatIsNotCopyableIsHandedOutAsStored() {
        FakeUnit marine = fake(Terran_Marine, 10);

        world(1, fakeOurs(marine), fakeEnemies(), () -> {
            Cache<FakeUnit> cache = new Cache<>();

            FakeUnit first = cache.get("marine", 100, () -> Select.our().first());
            FakeUnit second = cache.get("marine", 100, () -> fail("cache hit expected, not a recompute"));

            assertSame(marine, first);
            assertSame(first, second, "only copyable values are copied");
        });
    }

    @Test
    public void aSelectionSaysItIsCopyableThroughTheKernelInterface() {
        world(1, fakeOurs(fake(Terran_Marine, 10), fake(Terran_Marine, 20)), fakeEnemies(), () -> {
            Selection selection = Select.our();

            assertTrue(selection instanceof Copyable, "the cache only copies what says so");
            Selection copy = selection.copy();
            assertNotSame(selection, copy);
            assertEquals(selection.size(), copy.size());
            assertEquals(selection.list().get(0).id(), copy.list().get(0).id());
        });
    }
}