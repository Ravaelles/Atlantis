package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.OverlapsOurUnfinishedBuilding;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A site overlapping our own warping building is never placeable.
 *
 * <p>Measured OpenBW `214926`: Gateway [94,111] picked on top of warping
 * [92,111] (4x3 footprints share two columns) - the spiral's guards all
 * missed it because the warping building has no construction anymore
 * (ProtossWarping finishes on warp start) and the engine occupancy answer
 * did not refuse it. The builder walked there, the engine refused, the
 * site cancelled, and two orders re-requested the same need twice.</p>
 *
 * <p>Live unit list, both engines: workers and minerals never block here
 * (that false positive died 2026-10-08) - only our own unfinished
 * buildings do.</p>
 */
@Tag("production")
public class WarpingBuildingBlocksOverlapTest extends WorldStubForTests {

    @Test
    public void overlappingWarpingGatewayBlocks() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 92, 111).setCompleted(false);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 20, 20);

        world(1, fakeOurs(warping, worker), fakeEnemies(), () -> {
            assertTrue(
                OverlapsOurUnfinishedBuilding.check(APosition.create(94, 111), AUnitType.Protoss_Gateway),
                "4x3 footprints [94,111] and [92,111] share two columns");
        });
    }

    @Test
    public void distantSiteStaysPlaceable() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 92, 111).setCompleted(false);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 20, 20);

        world(1, fakeOurs(warping, worker), fakeEnemies(), () -> {
            assertFalse(
                OverlapsOurUnfinishedBuilding.check(APosition.create(60, 60), AUnitType.Protoss_Gateway),
                "far tile must stay legal");
        });
    }

    @Test
    public void workersAndMineralsNeverBlock() {
        FakeUnit miner = fake(AUnitType.Protoss_Probe, 94, 111);
        FakeUnit patch = fake(AUnitType.Resource_Mineral_Field, 95, 111);

        world(1, fakeOurs(miner, patch), fakeEnemies(), () -> {
            assertFalse(
                OverlapsOurUnfinishedBuilding.check(APosition.create(94, 111), AUnitType.Protoss_Pylon),
                "a mining worker is transient - the 2026-10-08 false positive must not return");
        });
    }

    @Test
    public void finishedSiblingNextDoorStaysLegal() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 92, 111);
        FakeUnit worker = fake(AUnitType.Protoss_Probe, 20, 20);

        world(1, fakeOurs(standing, worker), fakeEnemies(), () -> {
            assertFalse(
                OverlapsOurUnfinishedBuilding.check(APosition.create(96, 111), AUnitType.Protoss_Gateway),
                "finished buildings are the engine's answer, not this guard's");
        });
    }
}
