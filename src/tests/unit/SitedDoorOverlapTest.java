package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.position.RequestBuildingNear;
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
 * The sited door bypasses the spiral checklist, so it asks the two
 * overlap questions itself. A tile an unfinished or a standing
 * same-type building covers dies downstream (refresh cancel /
 * proximity finish) while the caller re-fires blindly - measured
 * OpenBW: sited PhotonC [115,15] 21x. The door refuses it instead:
 * no order, no construction, no BUILD row for a doomed site.
 */
@Tag("placement")
public class SitedDoorOverlapTest extends WorldStubForTests {

    @Test
    public void standingCoverBlocks() {
        FakeUnit standing = fake(AUnitType.Protoss_Pylon, 118, 16);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            assertTrue(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(118, 17), AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void unfinishedOverlapBlocks() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 118, 16);
        warping.setCompleted(false);

        world(1, fakeOurs(warping), fakeEnemies(), () -> {
            assertTrue(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(118, 17), AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void packedNeighbourPasses() {
        FakeUnit standing = fake(AUnitType.Protoss_Pylon, 118, 16);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            assertFalse(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(119, 18), AUnitType.Protoss_Pylon));
            // Gateway 4x3 at [118,18] spans y18-20: disjoint from the
            // pylon's y16-17, touching at the edge - packed, legal.
            assertFalse(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(118, 18), AUnitType.Protoss_Gateway));
            assertFalse(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                null, AUnitType.Protoss_Pylon));
            assertFalse(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(118, 17), null));
        });
    }

    @Test
    public void differentTypeOverlapIsRefusedToo() {
        FakeUnit standing = fake(AUnitType.Protoss_Pylon, 118, 16);

        world(1, fakeOurs(standing), fakeEnemies(), () -> {
            // Gateway 4x3 at [118,17] spans y17-19, sharing the y=17 row
            // with the standing pylon - overlapping, not packed. The engine
            // refuses occupied footprints of any type, and so does the door
            // now (NEXT #192: CyberneticsC cancelled in a loop on exactly
            // this shape).
            assertTrue(RequestBuildingNear.isSiteBlockedByKnownBuilding(
                APosition.create(118, 17), AUnitType.Protoss_Gateway));
        });
    }
}
