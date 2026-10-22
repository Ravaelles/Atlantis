package tests.acceptance;

import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.requests.produce.ProduceBuilding;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-16, MANDATORY: a warped order must never be sited again.
 * Measured (Wine, CONSTRUCTION_REQUESTED twin): row #2 warped at [16,48]
 * at 1:20, then re-requested at [18,44] at 1:48 and 1:52 - warp-finish
 * releases the builder, so the live-builder guard reads "builderless"
 * and the same order goes out for a second site while its building is
 * still warping. The link is the order instance: a sibling row links a
 * different order and still proceeds.
 */
public class WarpedOrderIsNotResitedTest extends WorldStubForTests {

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
    public void ownWarpingBuildingBlocksResite() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 28, 20);
        warping.setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            warping,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            ProductionOrder row = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            warping.setProductionOrder(row);

            assertTrue(
                ProduceBuilding.orderHasWarpingBuilding(row, AUnitType.Protoss_Gateway),
                "row #2's building warps - row #2 gets no second site");
        });

        assertTrue(fired, "the frame was reached - else this test proves nothing");
    }

    @Test
    public void siblingRowStillProceeds() {
        FakeUnit warping = fake(AUnitType.Protoss_Gateway, 28, 20);
        warping.setCompleted(false);

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            warping,
            fake(AUnitType.Protoss_Probe, 21, 21)
        ), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3 || fired) return;

            fired = true;

            ProductionOrder rowTwo = new ProductionOrder(AUnitType.Protoss_Gateway, 10);
            ProductionOrder rowThree = new ProductionOrder(AUnitType.Protoss_Gateway, 12);
            warping.setProductionOrder(rowTwo);

            assertFalse(
                ProduceBuilding.orderHasWarpingBuilding(rowThree, AUnitType.Protoss_Gateway),
                "the warp belongs to row #2 - row #3 is a different order "
                    + "and must still site");
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
