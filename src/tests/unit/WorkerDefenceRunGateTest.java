package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Run-gate (owner 2026-11-09, timeout hunt): every handle() branch
 * needs an enemy within 12 tiles (reaver 11.4/12 is the longest;
 * helping needs the victim's attacker within 4.5 of a friend within
 * 2.2), while the cached selection holds 15 - so an empty 13-tile
 * read proves handle() would tour and return null. The wounded
 * in-peace worker was paying that full tour every frame; the gate
 * makes applies() answer it up front.
 */
@Tag("combat")
public class WorkerDefenceRunGateTest extends WorldStubForTests {

    @Test
    public void woundedWorkerWithNoEnemiesDoesNotRun() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 27, 20);
        probe.hp = 25;

        world(20, fakeOurs(nexus, probe), fakeEnemies(), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertFalse(new WorkerDefenceRun(worker).applies(),
                "no enemies within 13 tiles: handle() would tour and return null - skip it");
        });
    }

    @Test
    public void woundedWorkerWithRaiderNearStillRuns() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 27, 20);
        probe.hp = 25;
        FakeUnit ling = fake(AUnitType.Zerg_Zergling, 30, 20);
        ling.setEnemy();

        world(20, fakeOurs(nexus, probe), fakeEnemies(ling), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(new WorkerDefenceRun(worker).applies(),
                "raider 3 tiles out: the gate stays open, existing logic decides");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
