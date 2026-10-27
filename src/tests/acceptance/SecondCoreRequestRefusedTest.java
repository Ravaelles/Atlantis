package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.OrderStatus;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-17: one Cybernetics Core per game - enforced at REQUEST
 * time, not just at creation. Measured (OpenBW 085140): row #9 warped at
 * 3:09, row #32 requested one frame later (3:09) and warped at 3:18, row
 * #33 at 3:23 - finish-removal vs re-request races slip past every
 * creation-time cap, because at request moment the older site is already
 * gone from the list while its building warps. A second Core order while
 * any Core comes (other order's site or warping building) is refused.
 */
public class SecondCoreRequestRefusedTest extends WorldStubForTests {

    private boolean fired = false;

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @AfterEach
    public void clean() {
        ConstructionRequests.constructions.clear();
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        fired = false;
    }

    @Test
    public void warpingCoreOfAnotherOrderBlocksRequest() {
        FakeUnit warping = fake(AUnitType.Protoss_Cybernetics_Core, 28, 20);
        warping.setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Gateway, 29, 20),
            warping,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 600;
            currentGas = 400;
            currentSupplyUsed = 22;
            currentSupplyTotal = 25;

            // Row #9 warped a frame ago: no site anymore, building warping.
            ProductionOrder rowNine = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22);
            rowNine.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, rowNine);
            warping.setProductionOrder(rowNine);

            // Row #32, dynamic, fresh - the duplicate.
            ProductionOrder rowDynamic = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22);
            Queue.get().addNew(0, rowDynamic);
            Queue.get().clearCache();

            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Cybernetics_Core, rowDynamic),
                "row #9 warps - row #32 must not even pick a site");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void sitedCoreOfAnotherOrderBlocksRequest() {
        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Gateway, 29, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 600;
            currentGas = 400;
            currentSupplyUsed = 22;
            currentSupplyTotal = 25;

            // Row #9 dispatched, builder walking to the site.
            ProductionOrder rowNine = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22);
            rowNine.setStatus(OrderStatus.IN_PROGRESS);
            Queue.get().addNew(0, rowNine);
            Construction site = new Construction(AUnitType.Protoss_Cybernetics_Core);
            rowNine.setConstruction(site);
            site.setProductionOrder(rowNine);
            ConstructionRequests.constructions.add(site);

            ProductionOrder rowDynamic = new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 22);
            Queue.get().addNew(0, rowDynamic);
            Queue.get().clearCache();

            assertFalse(
                ProduceBuilding.produceBuilding(AUnitType.Protoss_Cybernetics_Core, rowDynamic),
                "row #9 sited - row #32 must not pick a second site");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }
}
