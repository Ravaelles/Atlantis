package tests.unit;

import atlantis.placement.core.BuildLocation;
import atlantis.placement.core.BuildLocationRanker;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Home area outranks foreign ground on availability ties (owner order:
 * condense near the main, Stardust doctrine). Power still rules above
 * this level - a powered foreign tile beats a dark home one - so the
 * Gateway fast path (powered anywhere, now) is untouched; only ties
 * condense.
 */
@Tag("placement")
public class RankerSameAreaTest {

    private static BuildLocation loc(int tx, int ty, int avail, boolean sameArea) {
        return new BuildLocation(tx, ty, 2, 2, 0, avail, 0, false)
            .refined(0, 0, sameArea);
    }

    @Test
    public void sameAreaBeatsForeignOnAvailabilityTie() {
        BuildLocationRanker ranker = new BuildLocationRanker(false, false);

        assertTrue(ranker.compare(loc(10, 10, 0, true), loc(50, 50, 0, false)) < 0,
            "home tile first on tie");
        assertTrue(ranker.compare(loc(50, 50, 0, false), loc(10, 10, 0, true)) > 0,
            "foreign tile last on tie");
    }

    @Test
    public void availabilityStillBeatsArea() {
        BuildLocationRanker ranker = new BuildLocationRanker(false, false);

        assertTrue(ranker.compare(loc(50, 50, 0, false), loc(10, 10, 500, true)) < 0,
            "powered foreign now beats dark home later - speed over tidiness");
    }

    @Test
    public void neverAvailableSortsLastRegardlessOfArea() {
        BuildLocationRanker ranker = new BuildLocationRanker(false, false);

        assertTrue(ranker.compare(loc(10, 10, -1, true), loc(50, 50, 9999, false)) > 0,
            "never-available home sorts after any real frame");
    }
}
