package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.v2.engine.GameStateSnapshot;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * The V2 coming-supply count must see constructions, not just units: a
 * NOT_STARTED or STARTING Pylon has no unit yet, so the unit-only count
 * reads zero and the goal re-fires every frame until the first warp -
 * the scheduler then issues a second Pylon behind the walking builder
 * (measured V2 LIVE: Pylon@4234 + Pylon@4341 107f apart into one
 * block). PREPARING picks stay out (aimed future); IN_PROGRESS is
 * unit-covered.
 */
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
}
