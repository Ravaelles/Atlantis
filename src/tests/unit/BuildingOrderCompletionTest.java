package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * A building order is FINISHED when its building stands completed.
 *
 * <p>
 * Measured need (owner, blocked-at-41 Wine game): the production log showed
 * Pylons and Gateways "in production" that finished minutes ago - because no
 * code path ever marked a building order complete (this method answered for
 * tech/upgrades only), so every building order sat IN_PROGRESS forever. A
 * stale IN_PROGRESS clogs the counts the duplicate guards read and lies in
 * the log.
 * </p>
 */
@Tag("production")
public class BuildingOrderCompletionTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @AfterEach
    public void cleanConstructions() {
        ConstructionRequests.constructions.clear();
    }

    @Test
    public void builtPylonCompletesItsOrder() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 24, 24);

        world(30, fakeOurs(nexus, pylon), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 10) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(fake(AUnitType.Protoss_Probe, 25, 25));
            construction.setPositionToBuild(APosition.create(24, 24));
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            construction.setBuildingUnit(pylon);
            ConstructionRequests.constructions.add(construction);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
            order.setConstruction(construction);
            Queue.get().addNew(0, order);

            Queue.get().refresh();

            assertEquals(
                OrderStatus.FINISHED, order.status(),
                "a standing completed Pylon completes its order - IN_PROGRESS"
                    + " forever is the clog the production log showed");
        });
    }

    @Test
    public void warpingPylonDoesNotCompleteItsOrder() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 24, 24);
        warping.setCompleted(false);

        world(30, fakeOurs(nexus, warping), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            Construction construction = new Construction(AUnitType.Protoss_Pylon);
            construction.setBuilder(fake(AUnitType.Protoss_Probe, 25, 25));
            construction.setPositionToBuild(APosition.create(24, 24));
            construction.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            construction.setBuildingUnit(warping);
            ConstructionRequests.constructions.add(construction);

            ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 0);
            order.setConstruction(construction);
            Queue.get().addNew(0, order);

            Queue.get().refresh();

            assertNotEquals(
                OrderStatus.FINISHED, order.status(),
                "a warping Pylon is not a built Pylon - completion is the"
                + " standing building, not the warp start");
        });
    }
}
