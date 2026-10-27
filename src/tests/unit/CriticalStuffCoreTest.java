package tests.unit;

import atlantis.production.dynamic.protoss.prioritize.ProtossCriticalStuffInQueue;
import atlantis.production.orders.production.queue.add.AddToQueue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A queued Cybernetics Core pauses the worker pump below 250 minerals.
 *
 * <p>
 * Measured need (owner's Wine game): with no reservation the worker pump ate
 * every bank and the Core waited until a supply block accidentally stopped
 * workers - the Core is the tech the early game pivots on, so it joins Robo
 * and Cannon on the critical list. Below 250 with a Core queued, workers
 * wait; without one, or rich, business as usual.
 * </p>
 */
@Tag("production")
public class CriticalStuffCoreTest extends AbstractTestWithWorld {

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    @Test
    public void queuedCorePausesWorkersWhenPoor() {
        currentMinerals = 100;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            AddToQueue.withTopPriority(AUnitType.Protoss_Cybernetics_Core);

            assertFalse(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "a queued Core below 250 minerals must pause the worker pump");
        });
    }

    @Test
    public void noCoreQueuedMeansBusinessAsUsual() {
        currentMinerals = 100;

        world(5, fakeOurs(fake(AUnitType.Protoss_Nexus, 20, 20)), fakeEnemies(), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                ProtossCriticalStuffInQueue.hasEnoughMinerals(),
                "nothing critical queued - the pump decides on its own gates");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
