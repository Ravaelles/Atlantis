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
 * A travelling builder is live demand, not an idle order (owner
 * 2026-10-19: STARTING counts as started everywhere demand is counted -
 * except the hand-written door guard in {@code ProduceBuilding}, which
 * names only NOT_STARTED and IN_PROGRESS). While STARTING is unnamed
 * there, a repeat request for an order whose builder is walking mints a
 * second construction instead of waiting - the duplicate-per-order
 * class behind the Wine 39x-Gateway DECISIONS storm (oid 72: every
 * BUILD row paired with a fresh cid, same tile, same-frame CANCEL).
 */
public class LiveConstructionGuardTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
    }

    private ProductionOrder orderLinkedTo(ConstructionOrderStatus status, FakeUnit builder) {
        ProductionOrder order = new ProductionOrder(AUnitType.Protoss_Gateway, 30);
        Construction construction = new Construction(AUnitType.Protoss_Gateway);
        construction.setPositionToBuild(APosition.create(116, 19));
        construction.setStatus(status);
        construction.setBuilder(builder);
        construction.setProductionOrder(order);
        ConstructionRequests.constructions.add(construction);
        return order;
    }

    @Test
    public void startingWithLiveBuilderIsLive() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            assertTrue(ProduceBuilding.hasLiveConstruction(orderLinkedTo(
                ConstructionOrderStatus.STARTING, builder)));
        });
    }

    @Test
    public void notStartedWithLiveBuilderIsLive() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            assertTrue(ProduceBuilding.hasLiveConstruction(orderLinkedTo(
                ConstructionOrderStatus.NOT_STARTED, builder)));
        });
    }

    @Test
    public void inProgressWithLiveBuilderIsLive() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 92, 111);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            assertTrue(ProduceBuilding.hasLiveConstruction(orderLinkedTo(
                ConstructionOrderStatus.IN_PROGRESS, builder)));
        });
    }

    @Test
    public void startingWithDeadBuilderIsNotLive() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            builder.setHp(0);
            assertFalse(ProduceBuilding.hasLiveConstruction(orderLinkedTo(
                ConstructionOrderStatus.STARTING, builder)));
        });
    }

    @Test
    public void preparingIsNotLiveDemand() {
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(1, fakeOurs(builder), fakeEnemies(), () -> {
            assertFalse(ProduceBuilding.hasLiveConstruction(orderLinkedTo(
                ConstructionOrderStatus.PREPARING, builder)));
        });
    }

    @Test
    public void unlinkedOrderIsNotLive() {
        world(1, fakeOurs(fake(AUnitType.Protoss_Probe, 30, 20)), fakeEnemies(), () -> {
            assertFalse(ProduceBuilding.hasLiveConstruction(
                new ProductionOrder(AUnitType.Protoss_Gateway, 30)));
            assertFalse(ProduceBuilding.hasLiveConstruction(null));
        });
    }

    @Test
    public void requestRefusesWhileBuilderTravels() {
        currentMinerals = 500;
        currentSupplyUsed = 33;
        currentSupplyTotal = 41;
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit builder = fake(AUnitType.Protoss_Probe, 30, 20);

        world(5, fakeOurs(nexus, builder), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = orderLinkedTo(ConstructionOrderStatus.STARTING, builder);
            int before = ConstructionRequests.constructions.size();

            assertFalse(
                ProduceBuilding.requestBuilding(AUnitType.Protoss_Gateway, order),
                "travelling builder is live demand - wait, do not mint a second site");
            assertEquals(before, ConstructionRequests.constructions.size(),
                "no duplicate construction for one order");
        });
    }
}
