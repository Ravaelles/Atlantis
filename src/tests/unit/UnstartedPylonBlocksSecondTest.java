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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * PurpleWave rule (owner 2026-10-19): one unstarted Pylon at a time.
 * A NOT_STARTED or STARTING Pylon construction for another order means
 * supply is already on the way with no unit to show for it yet -
 * ordering a second one behind the walking builder is the 30/48
 * toward 38/65 slide. Own traces are excluded (replacement proceeds);
 * PREPARING picks are excluded (aimed future, never a block cause).
 */
@Tag("production")
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

    @Test
    public void aboveSixtyTwoUnstartedAllowedThirdRefused() {
        currentMinerals = 500;
        currentSupplyUsed = 64;
        currentSupplyTotal = 68;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(5, fakeOurs(nexus, builder), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder first = pylonOrderLinkedTo(ConstructionOrderStatus.STARTING);
            first.construction().setBuilder(builder);

            assertFalse(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 62)),
                "past 60 supply the second concurrent Pylon is allowed");

            ProductionOrder second = pylonOrderLinkedTo(ConstructionOrderStatus.STARTING);
            second.construction().setBuilder(builder);

            assertTrue(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 62)),
                "past 60 supply the third concurrent Pylon still waits");
        });
    }

    @Test
    public void echoInProgressWithoutUnitBlocks() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            // IN_PROGRESS on the order echo, server-side warp not yet
            // registered: no unit exists, so neither the warping-unit
            // check nor the not-started set sees it (measured: 65f-apart
            // warp pair behind one travelling builder).
            ProductionOrder other = new ProductionOrder(AUnitType.Protoss_Pylon, 25);
            Construction echoing = new Construction(AUnitType.Protoss_Pylon);
            echoing.setPositionToBuild(APosition.create(112, 7));
            echoing.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            echoing.setBuilder(builder);
            echoing.setProductionOrder(other);
            ConstructionRequests.constructions.add(echoing);

            assertTrue(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 27)));
        });
    }

    @Test
    public void linkedWarpingBuildingDoesNotBlock() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);
        FakeUnit warping = fake(AUnitType.Protoss_Pylon, 18, 46);

        world(1, fakeOurs(builder, warping), fakeEnemies(), () -> {
            // The unit registered: the pipeline may keep one more going,
            // capped by pylon-cap, not by this guard.
            ProductionOrder other = new ProductionOrder(AUnitType.Protoss_Pylon, 25);
            Construction working = new Construction(AUnitType.Protoss_Pylon);
            working.setPositionToBuild(APosition.create(112, 7));
            working.setStatus(ConstructionOrderStatus.IN_PROGRESS);
            working.setBuilder(builder);
            working.setBuildingUnit(warping);
            working.setProductionOrder(other);
            ConstructionRequests.constructions.add(working);

            assertFalse(
                ProduceBuilding.anotherUnstartedPylonExists(new ProductionOrder(AUnitType.Protoss_Pylon, 27)));
        });
    }
}
