package tests.unit;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.constructions.commanders.CancelTooLongConstructions;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.order.ProductionOrderPriority;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner's Wine game: builder #5 walked 48 s to a 10-tile Pylon site, got
 * cancelled for age, yet the engine accepted his order 5 s later. A builder
 * on foot is progress, not a stall. And the post-cancel rescue died in the
 * dedup wall while the game had zero Pylons (### FAILED ### null).
 */
@Tag("production")
public class WalkingBuilderNotStalledTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Test
    public void movingBuilderShieldsAgedConstruction() {
        currentMinerals = 600;
        currentSupplyUsed = 9;
        currentSupplyTotal = 17;

        FakeUnit walker = fake(AUnitType.Protoss_Probe, 21, 21);
        walker.lastCommand = "Move";

        world(700, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            walker
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() == 5) {
                ConstructionRequests.constructions.clear();
                Construction walking = new Construction(AUnitType.Protoss_Pylon);
                walking.setBuilder(walker);
                walking.setPositionToBuild(atlantis.map.position.APosition.create(90, 112));
                ConstructionRequests.addConstruction(walking);
            }
            if (atlantis.game.A.now() != 690) return;

            Construction walking = ConstructionRequests.all().stream()
                .filter(c -> c.buildingType().equals(AUnitType.Protoss_Pylon))
                .findFirst().orElse(null);
            assertNotNull(walking, "sanity: the construction is still listed");
            CancelTooLongConstructions.cancelCauseTakingTooLongIfNeeded(
                walking, AUnitType.Protoss_Pylon, AUnitType.Protoss_Pylon);

            assertTrue(
                ConstructionRequests.constructions.contains(walking),
                "a builder on foot converges - age must not cancel him");
        });
    }

    @Test
    public void rescueBypassesDedup() {
        currentMinerals = 400;
        currentSupplyUsed = 9;
        currentSupplyTotal = 9;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();
            ConstructionRequests.constructions.clear();

            ProductionOrder rescued = CancelTooLongConstructions
                .forceRescueOrder(AUnitType.Protoss_Pylon);

            assertNotNull(rescued, "rescue must go through with zero Pylons standing");
            assertEquals(ProductionOrderPriority.HIGH, rescued.priority());
            assertTrue(
                Queue.get().allOrders().list().contains(rescued),
                "rescue lands in the queue");
        });
    }
}
