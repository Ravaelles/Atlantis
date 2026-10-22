package tests.unit;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.IsReadyToProduceOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * What the readiness gate actually guarantees (measured, not assumed).
 *
 * <p>The gate is supply-side: a row goes READY up to two supplies before
 * its threshold when rich ({@code +1} nearness bonus for buildings at
 * supply 9+, {@code +1} dispatch margin for near or positionless sites;
 * the Gateway row below 9 is additionally held by its 234-mineral rule).
 * Minerals are NOT gated here for near sites - affordability is enforced
 * downstream at dispatch ({@code ShouldNotTravelToConstructYet}) and at
 * issue, which {@code FirstGatewayTimingTest} pins end to end. The
 * parameter is even misnamed ({@code isFarFromMainBaseSoTravelEarly}
 * carries the negation), but the behavior is coherent: near assumes the
 * builder arrives instantly, far ties up capital early.</p>
 *
 * <p>These pins call the gate directly (no frames, no engine), so timing
 * work never needs an end-to-end run for the decision half. The
 * mineral-at-issue half stays in the E2E tier.</p>
 */
@Tag("production")
public class ReadyToProduceTimingTest extends AbstractTestWithWorld {

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
    public void gatewayHeldBelowNineReadyAtNineRegardlessOfBank() {
        world(5, fixture(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = queued(new ProductionOrder(AUnitType.Protoss_Gateway, 9));

            currentSupplyUsed = 8;
            currentMinerals = 150;
            assertFalse(
                IsReadyToProduceOrder.isReady(order),
                "supply 8 for At 9 while poor: the 234-mineral rule holds below 9");

            currentMinerals = 500;
            assertTrue(
                IsReadyToProduceOrder.isReady(order),
                "supply 8 but rich: the 234 rule opens early by design (travel-early)");

            currentSupplyUsed = 9;
            currentMinerals = 0;
            assertTrue(
                IsReadyToProduceOrder.isReady(order),
                "at supply 9 the Gateway row is READY even broke - minerals gate at dispatch, not here");
        });
    }

    @Test
    public void pylonReadyTwoEarlyWhenRich() {
        world(5, fixture(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = queued(new ProductionOrder(AUnitType.Protoss_Pylon, 13));

            currentSupplyUsed = 10;
            currentMinerals = 500;
            assertFalse(
                IsReadyToProduceOrder.isReady(order),
                "supply 10 for At 13: below even the early margins");

            currentSupplyUsed = 11;
            assertTrue(
                IsReadyToProduceOrder.isReady(order),
                "at supply 11 with bank the Pylon row is READY (+1 nearness, +1 margin)");
        });
    }

    @Test
    public void coreReadyAtSixteenWhenRichNeverAtFifteen() {
        world(5, withGateway(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = queued(new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18));

            currentSupplyUsed = 15;
            currentMinerals = 500;
            currentGas = 150;
            assertFalse(
                IsReadyToProduceOrder.isReady(order),
                "supply 15 for At 18: below the margins (the live 16-warp was churn, not readiness)");

            currentSupplyUsed = 16;
            assertTrue(
                IsReadyToProduceOrder.isReady(order),
                "at supply 16 with bank the Core row is READY through the early margins");
        });
    }

    private FakeUnit[] fixture() {
        return fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 23, 21)
        );
    }

    private FakeUnit[] withGateway() {
        return fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 22, 20),
            fake(AUnitType.Protoss_Gateway, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        );
    }

    private ProductionOrder queued(ProductionOrder order) {
        currentSupplyTotal = 25;
        currentGas = 150;
        Queue.get().allOrders().list().clear();
        Queue.get().clearCache();
        Queue.get().addNew(0, order);

        return order;
    }
}
