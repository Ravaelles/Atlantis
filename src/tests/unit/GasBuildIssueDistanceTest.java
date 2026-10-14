package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.builders.TravelToConstruct;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A worker next to a geyser must be close enough to ISSUE the refinery, even
 * though the refinery's origin tile is far.
 *
 * <p><b>The defect this pins (NEXT.md #70, measured on OpenBW
 * {@code 20261013_100226_short}):</b> the Assimilator at [117,4] was
 * "too long (34s)" with zero build attempts while every {@code canBuild} gate
 * read true. The builder plateaued at tile-distance 3.7 - past the 3.5 gas
 * threshold - because the threshold was compared against the distance to the
 * refinery's ORIGIN tile, and a 4x2 refinery's center sits ~2.2 tiles from its
 * origin. An adjacent worker always reads ~3.6 and never issues.</p>
 *
 * <p>The fix measures every type against the footprint center (what the worker
 * walks to) with a footprint-sized threshold, instead of the origin tile.
 * The gas case below is the original; the second case pins the general rule
 * for a 4x3 (owner call after the #56 hover: metric and target must agree).</p>
 */
public class GasBuildIssueDistanceTest {

    private static final APosition SITE = APosition.create(117, 4);

    @Test
    public void aWorkerNextToTheGeyserIsWithinIssueReach() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 120, 6);
        Construction gas = new Construction(AUnitType.Protoss_Assimilator);
        gas.setPositionToBuild(SITE);

        double tileDistance = probe.distTo(SITE);

        assertTrue(tileDistance > 3.5,
            "the fixture must reproduce the game: tile distance " + tileDistance + " misses the 3.5 threshold");

        double used = TravelToConstruct.distanceForBuildOrderIssue(
            probe, gas, AUnitType.Protoss_Assimilator
        );

        assertTrue(used <= 3.5,
            "gas measures to the footprint center, so the adjacent worker issues (was " + used + ")");
    }

    @Test
    public void wideBuildingsMeasureToTheCenterToo() {
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 120, 6);
        Construction gateway = new Construction(AUnitType.Protoss_Gateway);
        gateway.setPositionToBuild(SITE);

        double used = TravelToConstruct.distanceForBuildOrderIssue(
            probe, gateway, AUnitType.Protoss_Gateway);

        assertEquals(
            probe.distTo(gateway.positionToBuildCenter()), used, 0.001,
            "every type measures to the footprint center now - the worker"
                + " walks there, so the decision must read the same geometry");
        assertTrue(
            used < probe.distTo(SITE),
            "for a 4x3 the center is much nearer than the origin from here -"
                + " the old tile metric parked wide footprints past the line");
    }
}
