package tests.acceptance;

import atlantis.production.constructions.Construction;
import atlantis.production.constructions.ConstructionRequests;
import atlantis.production.dynamic.protoss.buildings.ProduceGateway;
import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Owner 2026-10-16, MANDATORY: the build order holds two Gateways, yet a
 * third construction was ordered at supply 12. Mechanism: the dynamic cap
 * in {@code ProduceGateway} counted only queued rows. Once both BO rows
 * dispatch to constructions, the queue shows zero Gateways while two are
 * outstanding (one warping, one sited) - and a rich bank re-opens the
 * cap, so the pump adds a third. Outstanding means queue + constructions
 * + warping buildings.
 */
public class ThirdGatewayBlockedWhileTwoOutstandingTest extends WorldStubForTests {

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
    public void twoDispatchedGatewaysBlockDynamicThird() {
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
            ConstructionRequests.constructions.clear();
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            currentMinerals = 600;
            currentSupplyUsed = 12;
            currentSupplyTotal = 17;

            // Both BO rows dispatched: BO #1 warping (the unit above),
            // BO #2 sited but not started - and the queue is empty.
            ConstructionRequests.constructions.add(new Construction(AUnitType.Protoss_Gateway));

            assertFalse(
                ProduceGateway.produce(),
                "two dispatched Gateways (warping + sited) with an empty "
                    + "queue are still two outstanding - no dynamic third "
                    + "at supply 12");
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
