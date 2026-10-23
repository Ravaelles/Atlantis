package tests.unit;

import atlantis.production.constructions.position.BuildSiteDistance;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-23 (NEXT #145), corrected 2026-10-24: the legacy chain
 * mints absurd tiles, but a literal 30-tile cap fights the spiral's
 * own contract (dynamic orders search radius 36) - in a crowded base
 * the only free tiles sit at ring edge 30-36. The cap is the wider of
 * the user cap and the order radius: absurd-only, never ring-edge.
 */
@Tag("production")
public class BuildSiteDistanceTest extends WorldStubForTests {

    @Test
    public void farSiteRejected() {
        APosition site = APosition.create(100, 20);
        APosition base = APosition.create(20, 20);

        assertTrue(BuildSiteDistance.isTooFar(site, base, 36),
            "80 tiles out is not built: " + site.groundDist(base));
    }

    @Test
    public void nearSitePasses() {
        APosition site = APosition.create(25, 22);
        APosition base = APosition.create(20, 20);

        assertFalse(BuildSiteDistance.isTooFar(site, base, 36),
            "a neighbouring site builds: " + site.groundDist(base));
    }

    @Test
    public void nullNeverBlocks() {
        APosition base = APosition.create(20, 20);

        assertFalse(BuildSiteDistance.isTooFar(null, base, 36), "no site: the caller owns that exit");
        assertFalse(BuildSiteDistance.isTooFar(APosition.create(100, 20), null, 36), "no base: the caller owns that exit");
    }

    @Test
    public void capHonorsTheWiderContract() {
        assertEquals(36, BuildSiteDistance.capFor(36),
            "a radius-36 order keeps its own ring edge - the guard is absurd-only");
        assertEquals(30, BuildSiteDistance.capFor(20),
            "a tighter order is already bound by the spiral - the user cap is the floor");
        assertEquals(30, BuildSiteDistance.capFor(-1),
            "an undefined radius falls back to the user cap");
    }

    @Test
    public void ringEdgeSiteSurvivesItsOwnContract() {
        // 33 ground tiles out: killed by a literal 30, kept by the
        // order's 36 - the crowded-base ring edge from the live storm.
        APosition site = APosition.create(53, 20);
        APosition base = APosition.create(20, 20);

        assertTrue(site.groundDist(base) > 30, "test setup: beyond the literal cap");
        assertFalse(BuildSiteDistance.isTooFar(site, base, BuildSiteDistance.capFor(36)),
            "ring-edge legit site survives under its own radius-36 contract");
    }
}
