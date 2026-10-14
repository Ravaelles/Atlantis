package tests.unit;

import atlantis.game.AGame;
import atlantis.production.orders.production.queue.Queue;
import atlantis.production.orders.production.queue.order.ProductionOrder;
import atlantis.production.orders.production.queue.updater.IsReadyToProduceOrder;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.aGame;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * NEXT #71: {@code cantAffordAndDidntExpandYet} reads inverted - it blocks
 * every order when the bank is ABOVE 550 late (700 s+) on one base, while
 * the name says "cant afford". The current shape never converges (rich ->
 * blocked -> bank grows -> still blocked); the negation saves for the
 * expansion when poor and releases once rich.
 *
 * <p>Runs inside a stub world because the queue singleton only exists after
 * game start; the queue is cleared first for determinism (same pattern as
 * PriorityWaitersTest). {@code A.seconds()} is frame-derived in production
 * ({@code AGame.calcSeconds}) but answers a stubbed 0 in tests, so the
 * 701 s clock is stubbed once at frame 3 - one registration, not per frame
 * (BUGS B-24).</p>
 */
public class LateOneBaseReadinessTest extends AbstractTestWithWorld {

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

    private FakeUnit[] oneBase() {
        return fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Pylon, 24, 20),
            fake(AUnitType.Protoss_Gateway, 28, 20),
            fake(AUnitType.Protoss_Probe, 21, 21)
        );
    }

    private void lateOneBase(int minerals) {
        currentMinerals = minerals;
        currentGas = 0;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;
        aGame.when(AGame::timeSeconds).thenReturn(701);
        Queue.get().allOrders().list().clear();
        Queue.get().history().clear();
        Queue.get().clearCache();
    }

    @Test
    public void richLateOneBaseKeepsProducing() {
        world(5, oneBase(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            lateOneBase(600);

            assertTrue(
                IsReadyToProduceOrder.isReady(new ProductionOrder(AUnitType.Protoss_Probe, 0)),
                "600 minerals at 701 s on one base must not halt production (gate says cant-afford)");
        });
    }

    @Test
    public void poorLateOneBaseSavesForExpansion() {
        world(5, oneBase(), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;
            lateOneBase(100);

            assertFalse(
                IsReadyToProduceOrder.isReady(new ProductionOrder(AUnitType.Protoss_Probe, 0)),
                "100 minerals at 701 s on one base must wait - save for the expansion");
        });
    }
}
