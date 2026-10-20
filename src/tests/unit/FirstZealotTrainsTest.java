package tests.unit;

import atlantis.production.orders.production.queue.Queue;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-15: the opening must make sense to ~15 supply, first
 * Zealots included. Characterization pin (not mutation-proven - it guards
 * the currently-correct path): with a standing Gateway, bank and free
 * supply, the pump issues a Zealot train through the real commander tree.
 * If this ever goes red, unit production itself is broken, whatever the
 * bank says.
 */
@Tag("production")
public class FirstZealotTrainsTest extends AbstractTestWithWorld {

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
    public void pumpIssuesFirstZealot() {
        currentMinerals = 300;
        currentSupplyUsed = 12;
        currentSupplyTotal = 25;

        world(150, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Gateway, 24, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21)
        ), fakeEnemies(fake(AUnitType.Zerg_Zergling, 60, 20)), () -> {
            // Drive the REAL production commander, exactly as the game loop
            // does (the harness runs no commanders by itself).
            (new atlantis.production.dynamic.protoss.ProtossDynamicUnitProductionCommander())
                .invokedCommander();

            // TRAIN is a static leak across tests - clear once so only this
            // world's trains count.
            if (atlantis.game.A.now() == 0) tests.fakes.FakeUnitData.TRAIN.clear();

            if (atlantis.game.A.now() != 140) return;
            Queue.get().allOrders().list().clear();
            Queue.get().clearCache();

            // Trains go to the global fallback sink in stub (units have no
            // engine object); per-gateway sinks stay empty by design.
            assertTrue(
                tests.fakes.FakeUnitData.TRAIN.contains(AUnitType.Protoss_Zealot),
                "standing Gateway + 300 bank + free supply must train the first Zealot by frame 140");
        });
    }
}
