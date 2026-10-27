package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A site a standing building covers can never build - the proximity
 * finish removes it the same frame. The pick-time guard must see the
 * same coverage, or the finder re-offers the dead tile every frame
 * (measured OpenBW: oid 72, 38 requests for covered [116,19],
 * 38 same-frame cancels, one BUILD row per cycle; live: CyberneticsC
 * at [91,111] cancelled in a loop against a different-type overlap the
 * old same-type-only guard could not see).
 */
@Tag("production")
public class OverlapsStandingBuildingTest extends WorldStubForTests {

    @Test
    public void coveredSiteIsSeen() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 114, 18);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            // 4x3 from [114,18] covers x114-117 y18-20: [116,19] is inside.
            assertTrue(OverlapsOurStandingBuilding.check(
                APosition.create(116, 19), AUnitType.Protoss_Gateway));
        });
    }

    @Test
    public void adjacentPackedSiblingStaysLegal() {
        FakeUnit standing = fake(AUnitType.Protoss_Pylon, 118, 16);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            // 2x2 footprints [119,18] vs [118,16]: touching range,
            // disjoint tiles - packed base, not coverage.
            assertFalse(OverlapsOurStandingBuilding.check(
                APosition.create(119, 18), AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void differentTypeOverlapIsAlsoCovered() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 114, 18);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            APosition site = APosition.create(116, 19);
            assertTrue(OverlapsOurStandingBuilding.check(site, AUnitType.Protoss_Pylon),
                "a different-type completed overlap kills the tile the same way");
            assertTrue(atlantis.units.BuildingTilesAreOccupied.checkBuildingsOnly(
                    site, AUnitType.Protoss_Pylon),
                "pick-time and adopt-time guards read one predicate now");
        });
    }

    @Test
    public void unfinishedOverlapWaits() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 114, 18);
        warping.setCompleted(false);

        world(1, fakeOurs(warping), fakeEnemies(), () -> {
            assertFalse(OverlapsOurStandingBuilding.check(
                APosition.create(116, 19), AUnitType.Protoss_Gateway));
        });
    }

    @Test
    public void emptyMapAccepts() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            assertFalse(OverlapsOurStandingBuilding.check(
                APosition.create(116, 19), AUnitType.Protoss_Gateway));
            assertFalse(OverlapsOurStandingBuilding.check(null, AUnitType.Protoss_Gateway));
            assertFalse(OverlapsOurStandingBuilding.check(
                APosition.create(116, 19), null));
        });
    }
}
