package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The V2 coming-supply count must see constructions, not just units: a
 * NOT_STARTED or STARTING Pylon has no unit yet, so the unit-only count
 * reads zero and the goal re-fires every frame until the first warp -
 * the scheduler then issues a second Pylon behind the walking builder
 * (measured V2 LIVE: Pylon@4234 + Pylon@4341 107f apart into one
 * block). PREPARING picks stay out (aimed future); IN_PROGRESS is
 * unit-covered.
 */
@Tag("production")
public class ComingSupplyCountsConstructionsTest extends WorldStubForTests {

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private void addConstruction(AUnitType type, ConstructionOrderStatus status, int x, int y) {
        Construction construction = new Construction(type);
        construction.setPositionToBuild(APosition.create(x, y));
        construction.setStatus(status);
        ConstructionRequests.constructions.add(construction);
    }

    @Test
    public void notStartedAndStartingCountAsComing() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            assertEquals(0, GameStateSnapshot.unstartedProvidersComing(AUnitType.Protoss_Pylon));

            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.NOT_STARTED, 92, 113);
            assertEquals(1, GameStateSnapshot.unstartedProvidersComing(AUnitType.Protoss_Pylon));

            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.STARTING, 96, 113);
            assertEquals(2, GameStateSnapshot.unstartedProvidersComing(AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void preparingInProgressAndOtherTypesDoNotCount() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.PREPARING, 92, 113);
            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.IN_PROGRESS, 96, 113);
            addConstruction(AUnitType.Protoss_Gateway, ConstructionOrderStatus.NOT_STARTED, 86, 110);

            assertEquals(
                0, GameStateSnapshot.unstartedProvidersComing(AUnitType.Protoss_Pylon));
            assertEquals(
                0, GameStateSnapshot.unstartedProvidersComing(null));
        });
    }

    @Test
    public void inProgressWithoutLinkedBuildingCountsAsComing() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            // The warp was accepted but the unit has not registered yet:
            // neither the unit term nor the unstarted term sees it.
            Construction warping = new Construction(AUnitType.Protoss_Pylon);
            warping.setPositionToBuild(APosition.create(92, 113));
            warping.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            warping.setBuilder(builder);
            ConstructionRequests.constructions.add(warping);
            assertEquals(
                1, GameStateSnapshot.unmaterializedInProgressComing(AUnitType.Protoss_Pylon));

            // Once the building links, the unit term covers it - no double count.
            warping.setBuildingUnit(fake(AUnitType.Protoss_Pylon, 92, 113));
            assertEquals(
                0, GameStateSnapshot.unmaterializedInProgressComing(AUnitType.Protoss_Pylon));
            assertEquals(
                0, GameStateSnapshot.unmaterializedInProgressComing(null));
        });
    }

    @Test
    public void compositionCountsUnitsAndConstructionsOnce() {
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 92, 113);
        warping.setCompleted(false);

        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 30, 20),
            warping
        ), fakeEnemies(), () -> {
            // One warping unit, no constructions: the unit term alone.
            assertEquals(1, GameStateSnapshot.supplyProvidersComing(AUnitType.Protoss_Pylon));

            // The same warp linked to its construction: still 1, never 2.
            Construction linked = new Construction(AUnitType.Protoss_Pylon);
            linked.setPositionToBuild(APosition.create(92, 113));
            linked.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            linked.setBuildingUnit(warping);
            ConstructionRequests.constructions.add(linked);
            assertEquals(1, GameStateSnapshot.supplyProvidersComing(AUnitType.Protoss_Pylon));

            // Plus one travelling with no unit: 2 providers coming.
            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.STARTING, 96, 113);
            assertEquals(2, GameStateSnapshot.supplyProvidersComing(AUnitType.Protoss_Pylon));
        });
    }

    @Test
    public void unlinkedWarpingCountsInTheSum() {
        world(1, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 30, 20)
        ), fakeEnemies(), () -> {
            // No units at all, one IN_PROGRESS with no linked building:
            // the lag window the unit term and the unstarted term both miss.
            // Without the lag term in the sum this reads 0 and the goal
            // re-fires behind the warping Pylon.
            addConstruction(AUnitType.Protoss_Pylon, ConstructionOrderStatus.IN_PROGRESS, 92, 113);
            assertEquals(1, GameStateSnapshot.supplyProvidersComing(AUnitType.Protoss_Pylon));
        });
    }
}
