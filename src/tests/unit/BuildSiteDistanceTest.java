package tests.unit;

import atlantis.production.constructions.position.BuildSiteDistance;
import atlantis.map.position.APosition;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-26 (NEXT #145): the legacy chain mints absurd tiles that
 * cancel same-frame (463 same-tile CANCELs in one game). Until the
 * catalogue owns siting, no standard building breaks ground further
 * than 30 ground tiles from its search base.
 */
@Tag("production")
public class BuildSiteDistanceTest extends WorldStubForTests {

    @Test
    public void farSiteRejected() {
        APosition site = APosition.create(100, 20);
        APosition base = APosition.create(20, 20);

        assertTrue(BuildSiteDistance.isTooFar(site, base),
            "80 tiles out is not built: " + site.groundDist(base));
    }

    @Test
    public void nearSitePasses() {
        APosition site = APosition.create(25, 22);
        APosition base = APosition.create(20, 20);

        assertFalse(BuildSiteDistance.isTooFar(site, base),
            "a neighbouring site builds: " + site.groundDist(base));
    }

    @Test
    public void nullNeverBlocks() {
        APosition base = APosition.create(20, 20);

        assertFalse(BuildSiteDistance.isTooFar(null, base), "no site: the caller owns that exit");
        assertFalse(BuildSiteDistance.isTooFar(APosition.create(100, 20), null), "no base: the caller owns that exit");
    }
}
