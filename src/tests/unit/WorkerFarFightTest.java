package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits;
import atlantis.units.workers.defence.fight.WorkerDefenceHelpCannon;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Owner 2026-10-26 (Zealot Hell run-by corridor): workers never walk out
 * to fight combat units far from base. Entry gates read the worker's
 * position, but the attack order pursues - so the ban reads the ENEMY's
 * address at the shared last gate, per frame, for both fight paths, and
 * the cannon-help path (direct attacks, no shared gate) gets the same
 * 15-tile line on the worker.
 */
@Tag("combat")
public class WorkerFarFightTest extends WorldStubForTests {

    @Test
    public void swarmDoesNotChaseRaiderFarFromBase() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 27.5, 20);
        probe.hp = 35;
        FakeUnit mate = fake(AUnitType.Protoss_Probe, 28, 20);
        mate.hp = 35;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe, mate), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(new WorkerDefenceFightCombatUnits(worker).applies(),
                "the setup must reach the shared gate, not die in applies()");
            assertTrue(!new WorkerDefenceFightCombatUnits(worker).forceHandled(),
                "a raider 16 tiles from base is not ours to chase, swarm or not");
        });
    }

    @Test
    public void swarmStillFightsRaiderAtBase() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 27.5, 20);
        probe.hp = 35;
        FakeUnit mate = fake(AUnitType.Protoss_Probe, 28, 20);
        mate.hp = 35;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 25, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe, mate), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(new WorkerDefenceFightCombatUnits(worker).forceHandled(),
                "a raider at the base is still swarmed");
        });
    }

    @Test
    public void cannonHelpStandsDownFarFromBase() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 60, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 59, 20);
        probe.hp = 30;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 61, 20);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 61.5, 20);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 62, 20);
        l1.setEnemy();
        l2.setEnemy();
        l3.setEnemy();

        world(20, fakeOurs(nexus, cannon, probe), fakeEnemies(l1, l2, l3), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(!new WorkerDefenceHelpCannon(worker).applies(),
                "no worker walks 40 tiles to a forward cannon's fight");
        });
    }

    @Test
    public void cannonHelpStillFightsAtBase() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 25, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 24, 20);
        probe.hp = 30;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 26, 20);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 26.5, 20);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 27, 20);
        l1.setEnemy();
        l2.setEnemy();
        l3.setEnemy();

        world(20, fakeOurs(nexus, cannon, probe), fakeEnemies(l1, l2, l3), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(new WorkerDefenceHelpCannon(worker).applies(),
                "the home cannon keeps its worker support");
        });
    }
}
