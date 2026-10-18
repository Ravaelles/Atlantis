package tests.unit;

import atlantis.map.position.APosition;
import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionOrderStatus;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * PurpleWave rule (owner 2026-10-19): one unstarted Pylon at a time.
 * A NOT_STARTED or STARTING Pylon construction for another order means
 * supply is already on the way with no unit to show for it yet -
 * ordering a second one behind the walking builder is the 30/48
 * toward 38/65 slide. Own traces are excluded (replacement proceeds);
 * PREPARING picks are excluded (aimed future, never a block cause).
 */
public class UnstartedPylonBlocksSecondTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private ProductionOrder pylonOrderLinkedTo(ConstructionOrderStatus status) {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Pylon, 8);
        Construction construction = new Construction(AUnitType.Protoss_Pylon);
        construction.setPositionToBuild(APosition.create(92, 113));
        construction.setStatus(status);
        construction.setProductionOrder(order);
        ConstructionRequests.constructions.add(construction);
        return order;
    }

    @Test
    public void notStartedForAnotherOrderBlocks() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            pylonOrderLinkedTo(ConstructionOrderStatus.NOT_STARTED);

            assertTrue(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 15)));
        });
    }

    @Test
    public void startingForAnotherOrderBlocks() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            ProductionOrder other = pylonOrderLinkedTo(ConstructionOrderStatus.STARTING);
            other.construction().setBuilder(builder);

            assertTrue(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 15)));
        });
    }

    @Test
    public void ownTraceNeverBlocksItself() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            ProductionOrder own = pylonOrderLinkedTo(ConstructionOrderStatus.NOT_STARTED);

            assertFalse(ProduceBuilding.anotherUnstartedPylonExists(own),
                "replacing your own lost construction must proceed");
        });
    }

    @Test
    public void orderlessRequestIsConservative() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            pylonOrderLinkedTo(ConstructionOrderStatus.NOT_STARTED);

            assertTrue(ProduceBuilding.anotherUnstartedPylonExists(null),
                "with no order to attribute, an unstarted Pylon blocks");
        });
    }

    @Test
    public void preparingAndOtherTypesDoNotBlock() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            pylonOrderLinkedTo(ConstructionOrderStatus.PREPARING);

            ProductionOrder gatewayOrder = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            Construction gateway = new Construction(AUnitType.Protoss_Gateway);
            gateway.setStatus(ConstructionOrderStatus.NOT_STARTED);
            gateway.setProductionOrder(gatewayOrder);
            ConstructionRequests.constructions.add(gateway);

            assertFalse(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 15)));
        });
    }

    @Test
    public void secondPylonRequestRefusedWhileFirstTravels() {
        currentMinerals = 500;
        currentSupplyUsed = 25;
        currentSupplyTotal = 33;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(5, fakeOurs(nexus, builder), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder first = pylonOrderLinkedTo(ConstructionOrderStatus.STARTING);
            first.construction().setBuilder(builder);
            int before = ConstructionRequests.constructions.size();

            assertFalse(
                ProduceBuilding.requestBuilding(
                    AUnitType.Protoss_Pylon, new ProductionOrder(AUnitType.Protoss_Pylon, 27)),
                "one travelling Pylon covers the block - wait, do not mint a second site");
            assertEquals(before, ConstructionRequests.constructions.size(),
                "no duplicate construction for one supply need");
        });
    }
}
