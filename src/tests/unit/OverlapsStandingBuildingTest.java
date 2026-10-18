package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.conditions.OverlapsOurStandingBuilding;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A site a standing same-type building covers can never build - the
 * proximity finish removes it the same frame. The pick-time guard must
 * see the same coverage, or the finder re-offers the dead tile every
 * frame (measured OpenBW: oid 72, 38 requests for covered [116,19],
 * 38 same-frame cancels, one BUILD row per cycle).
 */
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
    public void differentTypeOverlapIsNotOurs() {
        FakeUnit standing = fake(AUnitType.Protoss_Gateway, 114, 18);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            assertFalse(OverlapsOurStandingBuilding.check(
                APosition.create(116, 19), AUnitType.Protoss_Pylon));
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
