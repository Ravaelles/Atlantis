package tests.unit.cache;

import atlantis.units.AUnitType;
import atlantis.util.cache.Cache;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Deferred defect #1 (see _AI/REVIEW.md §17): {@code getIfValid} used to
 * return stale-invalid units instead of recomputing them.
 */
public class CacheGetIfValidTest {

    @Test
    void validCachedUnitIsReturnedWithoutRecomputing() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);
        assertTrue(marine.isValid(), "test setup needs a valid unit");

        Cache<FakeUnit> cache = new Cache<>();
        AtomicInteger computations = new AtomicInteger();

        FakeUnit first = cache.getIfValid("key", 100, () -> {
            computations.incrementAndGet();
            return marine;
        });
        FakeUnit second = cache.getIfValid("key", 100, () -> {
            computations.incrementAndGet();
            return marine;
        });

        assertSame(marine, first);
        assertSame(marine, second);
        assertEquals(1, computations.get(), "valid entry must not recompute");
    }

    @Test
    void invalidCachedUnitIsDroppedAndRecomputed() {
        FakeUnit marine = new FakeUnit(AUnitType.Terran_Marine, 20, 20);
        FakeUnit replacement = new FakeUnit(AUnitType.Terran_Marine, 21, 20);

        Cache<FakeUnit> cache = new Cache<>();
        cache.set("key", 100, marine);

        marine.hp = -1;
        assertTrue(!marine.isValid(), "test setup needs an invalidated unit");

        FakeUnit result = cache.getIfValid("key", 100, () -> replacement);

        assertSame(replacement, result, "stale-invalid entry must be recomputed");
        assertSame(replacement, cache.get("key"), "fresh value must replace the stale one");
    }

    @Test
    void otherValueTypesPassThroughUntouched() {
        Cache<String> cache = new Cache<>();
        AtomicInteger computations = new AtomicInteger();

        String first = cache.getIfValid("key", 100, () -> {
            computations.incrementAndGet();
            return "value";
        });
        String second = cache.getIfValid("key", 100, () -> {
            computations.incrementAndGet();
            return "changed";
        });

        assertEquals("value", first);
        assertEquals("value", second);
        assertEquals(1, computations.get(), "plain values must keep old caching behaviour");
    }
}
