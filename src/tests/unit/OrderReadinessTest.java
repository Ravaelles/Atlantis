package tests.unit;

import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.IsReadyToProduceOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * When a queued building order becomes ready to produce.
 *
 * <p>
 * The Nexus order that sat in the queue from 6:19 to game end (20261013_114519)
 * with no REQUEST, no refusal and no cancel showed three indistinguishable
 * silences; the ORDER_NOT_READY probe now names the refusing term. This test
 * pins the two resource halves of that contract for a base order: rich means
 * ready-eligible, broke means waiting.
 * </p>
 */
public class OrderReadinessTest extends AbstractTestWithWorld {

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
    public void aBaseOrderWithBankIsReadyEligible() {
        currentMinerals = 800;

        world(10, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Pylon, 22, 22)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5) return;

            ProductionOrder order = new ProductionOrder(
                AUnitType.Protoss_Nexus, atlantis.map.position.APosition.create(40, 40), 0);

            assertTrue(
                IsReadyToProduceOrder.isReady(order),
                "a base order with bank must be ready-eligible (supply/resources/requirements all pass)");
        });
    }

    @Test
    public void aBaseOrderWithoutBankWaits() {
        currentMinerals = 0;

        world(10, fakeOurs(
                fake(AUnitType.Protoss_Nexus, 20, 20),
                fake(AUnitType.Protoss_Pylon, 22, 22)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 5) return;

            ProductionOrder order = new ProductionOrder(
                AUnitType.Protoss_Nexus, atlantis.map.position.APosition.create(40, 40), 0);

            assertFalse(
                IsReadyToProduceOrder.isReady(order),
                "a base order with an empty bank must wait (hasMinerals(310) fails)");
        });
    }
}
