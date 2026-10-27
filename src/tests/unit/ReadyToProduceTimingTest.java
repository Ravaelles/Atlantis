package tests.unit;

import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.ReadyToProduceOrder;
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
 * <p>Owner rule: at or past the gate the order builds, priority permitting;
 * below the gate it builds at most one supply early, and only with the
 * price plus one worker (50) in the bank. No lookahead, no travel bonus,
 * no nearness bonus. Minerals at the gate are NOT required - affordability
 * is enforced downstream at dispatch and at issue, which
 * {@code FirstGatewayTimingTest} pins end to end.</p>
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
                ReadyToProduceOrder.isReady(order),
                "supply 8 for At 9 while poor: the 234-mineral rule holds below 9");

            currentMinerals = 500;
            assertTrue(
                ReadyToProduceOrder.isReady(order),
                "supply 8 but rich: one early with price-plus-a-worker in the bank");

            currentSupplyUsed = 9;
            currentMinerals = 0;
            assertTrue(
                ReadyToProduceOrder.isReady(order),
                "at supply 9 the Gateway row is READY even broke - minerals gate at dispatch, not here");
        });
    }

    @Test
    public void pylonReadyOneEarlyWhenRichNeverTwoEarly() {
        world(5, fixture(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = queued(new ProductionOrder(AUnitType.Protoss_Pylon, 13));

            currentSupplyUsed = 11;
            currentMinerals = 500;
            assertFalse(
                ReadyToProduceOrder.isReady(order),
                "supply 11 for At 13: two early is too early, rich or not");

            currentSupplyUsed = 12;
            currentMinerals = 100;
            assertFalse(
                ReadyToProduceOrder.isReady(order),
                "supply 12 for At 13 but poor: one early needs price-plus-a-worker (150)");

            currentMinerals = 500;
            assertTrue(
                ReadyToProduceOrder.isReady(order),
                "supply 12 with bank: one early with 150 in hand");
        });
    }

    @Test
    public void coreReadyAtSeventeenWhenRichNeverAtSixteen() {
        world(5, withGateway(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            ProductionOrder order = queued(new ProductionOrder(AUnitType.Protoss_Cybernetics_Core, 18));

            currentSupplyUsed = 16;
            currentMinerals = 500;
            currentGas = 150;
            assertFalse(
                ReadyToProduceOrder.isReady(order),
                "supply 16 for At 18: two early never fires, the old margins are gone");

            currentSupplyUsed = 17;
            assertTrue(
                ReadyToProduceOrder.isReady(order),
                "supply 17 with bank: one early with price-plus-a-worker (250)");
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
