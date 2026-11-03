package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.ConstructionStatusChanger;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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
 * The engine places the warp off the requested site (measured across
 * 212 retained games: 3.16-5.0 tiles of drift on Gateway/Citadel/Nexus/
 * Pylon), so the warping self-match must cover the drift, not the
 * origin tile. 19 of 25 retained "builder gone" demotes fired on
 * healthy warps the 2.5-tile radius could not see (the Protoss builder
 * is correctly released on warp, so every miss demotes).
 *
 * <p>Pinned through the public demote seam: a drifted warp shields its
 * row, a neighbour's adopted warp shields nothing, and the same-tile
 * shape keeps working.</p>
 */
@Tag("production")
public class WarpingSelfMatchCoversDriftTest extends WorldStubForTests {

    private static final APosition SITE = APosition.create(40, 33);
    private static final APosition DRIFTED = APosition.create(44, 35);

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @BeforeEach
    public void hermetic() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    @AfterEach
    public void clean() {
        if (Queue.get() != null) {
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
        }
        ConstructionRequests.constructions.clear();
    }

    private FakeUnit warpingPylonAt(APosition tile) {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, tile.tx(), tile.ty());
        warping.setCompleted(false);
        warping.hp = 300;

        return warping;
    }

    private Construction inProgressRowAt(APosition site) {
        Construction row = new Construction(AUnitType.Protoss_Pylon);
        row.setPositionToBuild(site);
        row.setStatus(ConstructionOrderStatus.IN_PROGRESS);
        ConstructionRequests.addConstruction(row);

        return row;
    }

    @Test
    public void driftedWarpShieldsItsRow() {
        FakeUnit warping = warpingPylonAt(DRIFTED);
        final Construction[] row = new Construction[1];

        world(130, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            warping
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 2) {
                row[0] = inProgressRowAt(SITE);
            }
            if (atlantis.game.A.now() != 120) return;

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(row[0], null),
                "a warping building 4.5 tiles off its site is still this row's "
                    + "warp (measured drift reaches 5.0) - no builder-gone demote");
        });
    }

    @Test
    public void adoptedNeighborWarpShieldsNothing() {
        FakeUnit warping = warpingPylonAt(DRIFTED);
        final Construction[] row = new Construction[1];
        final Construction[] neighbor = new Construction[1];

        world(130, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            warping
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 2) {
                row[0] = inProgressRowAt(SITE);
                neighbor[0] = inProgressRowAt(APosition.create(60, 60));
                neighbor[0].setBuildingUnit(warping);
            }
            if (atlantis.game.A.now() != 120) return;

            assertTrue(
                ConstructionStatusChanger.demoteStalledInProgress(row[0], null),
                "the only nearby warp belongs to another row: a dead row must "
                    + "still demote instead of hiding behind its neighbour");
        });
    }

    @Test
    public void sameTileWarpStillShields() {
        FakeUnit warping = warpingPylonAt(SITE);
        final Construction[] row = new Construction[1];

        world(130, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            warping
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 2) {
                row[0] = inProgressRowAt(SITE);
            }
            if (atlantis.game.A.now() != 120) return;

            assertFalse(
                ConstructionStatusChanger.demoteStalledInProgress(row[0], null),
                "the zero-drift shape keeps working");
        });
    }
}
