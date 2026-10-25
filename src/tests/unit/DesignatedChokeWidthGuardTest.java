package tests.unit;

import atlantis.map.choke.AChoke;
import atlantis.map.choke.Chokes;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeChoke;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * NEXT #193: a main/natural designation wider than 10 tiles is not a
 * choke, it is a misread area (TauCross bottom start showed a giant
 * area nowhere near any base as the "main choke": the region-null
 * fallback picks the Euclidean-nearest choke with no width check).
 * Designations ours and enemy's alike refuse it; the narrow choke wins
 * even when the giant's center stands nearer to the base.
 */
@Tag("map")
public class DesignatedChokeWidthGuardTest {

    @Test
    public void giantAreaIsNotADesignatedChoke() {
        assertFalse(
            Chokes.isPlausibleDesignatedChoke(new FakeChoke(APosition.create(12, 105), 40)),
            "a 40-tile area is not a choke");
        assertFalse(
            Chokes.isPlausibleDesignatedChoke(new FakeChoke(APosition.create(20, 100), 10)),
            "10 tiles is not narrower than 10 tiles");
        assertFalse(Chokes.isPlausibleDesignatedChoke(null), "null designates nothing");
        assertTrue(
            Chokes.isPlausibleDesignatedChoke(new FakeChoke(APosition.create(20, 100), 3)),
            "a 3-tile choke designates");
    }

    @Test
    public void narrowChokeWinsOverNearerGiant() {
        APosition main = APosition.create(10, 110);
        AChoke giant = new FakeChoke(APosition.create(12, 105), 40);
        AChoke real = new FakeChoke(APosition.create(20, 100), 3);

        AChoke picked = Chokes.nearestNarrowChoke(main, Arrays.asList(giant, real));

        assertNotNull(picked, "a narrow choke exists, so something must designate");
        assertEquals(3, picked.width(), "the narrow choke wins, not the nearer giant");
    }

    @Test
    public void allWideMeansNoDesignation() {
        APosition main = APosition.create(10, 110);

        AChoke picked = Chokes.nearestNarrowChoke(
            main,
            Collections.singletonList(new FakeChoke(APosition.create(12, 105), 40)));

        assertNull(picked, "no narrow choke, no designation - callers fall back to anchor");
    }
}
