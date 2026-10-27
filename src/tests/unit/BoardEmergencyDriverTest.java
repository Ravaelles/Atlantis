package tests.unit;

import atlantis.production.dynamic.workers.AutoProduceWorkersCommander;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NEXT #91 driver (owner order 2A): emergency preemption only. An unseen
 * enemy with no detection pauses the worker pump unless the bank covers
 * both - everything else keeps legacy brakes (a top-only driver would
 * also kill Nexus saving, which lives at rank 80 by design).
 */
@Tag("infra")
public class BoardEmergencyDriverTest extends AbstractTestWithWorld {

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
    public void hiddenEnemyPausesPoorPump() {
        currentMinerals = 100;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 23, 21),
            fake(AUnitType.Protoss_Probe, 24, 21)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Dark_Templar, 60, 20)
        ), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertFalse(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "unseen DT with no detection and 100 bank must pause workers (save Forge)");
            assertEquals("BoardEmergency", AutoProduceWorkersCommander.REASON);
        });
    }

    @Test
    public void hiddenEnemyWithBankFlows() {
        currentMinerals = 300;
        currentSupplyUsed = 20;
        currentSupplyTotal = 40;

        world(5, fakeOurs(
            fake(AUnitType.Protoss_Nexus, 20, 20),
            fake(AUnitType.Protoss_Probe, 21, 21),
            fake(AUnitType.Protoss_Probe, 22, 21),
            fake(AUnitType.Protoss_Probe, 23, 21),
            fake(AUnitType.Protoss_Probe, 24, 21)
        ), fakeEnemies(
            fake(AUnitType.Protoss_Dark_Templar, 60, 20)
        ), () -> {
            if (atlantis.game.A.now() != 3) return;

            assertTrue(
                AutoProduceWorkersCommander.shouldProduceWorkers(),
                "300 bank covers worker + Forge: emergency does not starve economy, got: "
                    + AutoProduceWorkersCommander.REASON);
        });
    }
}
