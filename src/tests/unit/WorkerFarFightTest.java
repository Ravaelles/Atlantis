package tests.unit;

import atlantis.game.A;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import atlantis.units.select.Select;
import atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits;
import atlantis.units.workers.defence.fight.WorkerDefenceHelpCannon;
import atlantis.units.workers.defence.fight.WorkerDefenceStopFighting;
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
 * Owner 2026-10-26 (Zealot Hell run-by corridor; tightened to 10 on the
 * live report that workers trade far beyond the army): workers never
 * walk out to fight combat units far from base. Entry gates read the
 * worker's position, but the attack order pursues - so the ban reads the
 * ENEMY's address at the shared last gate, per frame, for both fight
 * paths, and the cannon-help path (direct attacks, no shared gate) gets
 * the same 10-tile line on the worker. An ongoing fight that drifted out
 * is stopped with the same named reason (WorkerDefenceStopFighting).
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
    public void swarmDoesNotChaseRaiderAt12Tiles() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 25, 20);
        probe.hp = 35;
        FakeUnit mate = fake(AUnitType.Protoss_Probe, 26, 20);
        mate.hp = 35;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe, mate), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(!new WorkerDefenceFightCombatUnits(worker).forceHandled(),
                "a raider 12 tiles from base is past the 10-tile line, swarm or not");
        });
    }

    @Test
    public void stopFightingWhenWorkerDriftedBeyond10Tiles() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 31, 20);
        probe.hp = 40;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 32, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            worker.attackUnit(zealot);
            assertTrue(new WorkerDefenceStopFighting(worker).applies(),
                "an attacking worker 11 tiles from base stops, even mid-fight");
            assertTrue(new WorkerDefenceStopFighting(worker).forceHandled(),
                "the stop executes instead of letting the pursuit continue");
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
    public void naturalStrayWorkerDoesNotFight() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit mainNexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit naturalNexus = fake(AUnitType.Protoss_Nexus, 35, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 34, 20);
        probe.hp = 35;
        FakeUnit mateA = fake(AUnitType.Protoss_Probe, 33, 21);
        mateA.hp = 35;
        FakeUnit mateB = fake(AUnitType.Protoss_Probe, 33, 19);
        mateB.hp = 35;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        zealot.setEnemy();

        world(20, fakeOurs(mainNexus, naturalNexus, probe, mateA, mateB), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(!atlantis.units.workers.defence.fight.WorkerDefenceFightCombatUnits.processFightEnemyCombatUnits(worker),
                "a swarm 16 tiles from the main does not fight at the natural, Nexus or not");
        });
    }

    @Test
    public void naturalStrayAttackerTurnsBack() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit mainNexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit naturalNexus = fake(AUnitType.Protoss_Nexus, 35, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 34, 20);
        probe.hp = 40;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 36, 20);
        zealot.setEnemy();

        world(20, fakeOurs(mainNexus, naturalNexus, probe), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            worker.attackUnit(zealot);
            assertTrue(new WorkerDefenceStopFighting(worker).applies(),
                "an attacking worker 14 tiles from the main stops, natural Nexus or not");
            assertTrue(new WorkerDefenceStopFighting(worker).forceHandled(),
                "the stop executes instead of letting the natural fight continue");
        });
    }

    @Test
    public void cannonHelpStandsDownAtFarNatural() {
        enemyRaceInWorld = Race.Zerg;

        FakeUnit mainNexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit naturalNexus = fake(AUnitType.Protoss_Nexus, 35, 20);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 36, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 34, 20);
        probe.hp = 30;
        FakeUnit l1 = fake(AUnitType.Zerg_Zergling, 37, 20);
        FakeUnit l2 = fake(AUnitType.Zerg_Zergling, 37.5, 20);
        FakeUnit l3 = fake(AUnitType.Zerg_Zergling, 38, 20);
        l1.setEnemy();
        l2.setEnemy();
        l3.setEnemy();

        world(20, fakeOurs(mainNexus, naturalNexus, cannon, probe), fakeEnemies(l1, l2, l3), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            assertTrue(!new WorkerDefenceHelpCannon(worker).applies(),
                "no worker walks 15 tiles to a natural cannon's fight");
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

    @Test
    public void unsupportedPursuitFarFromBaseWithdraws() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 32, 20);
        probe.hp = 40;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, probe), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            worker.move(zealot, Actions.MOVE_ATTACK, "SwarmMove");

            assertTrue(new WorkerDefenceStopFighting(worker).applies(),
                "a healthy worker pursuing 12 tiles from base, 12 from any "
                + "building, with no combat support, withdraws instead of "
                + "donating - pursuit without support is the reported death");
            assertTrue(new WorkerDefenceStopFighting(worker).forceHandled(),
                "the withdraw executes instead of letting the pursuit continue");
        });
    }

    @Test
    public void supportedPursuitNearBuildingsKeepsFighting() {
        enemyRaceInWorld = Race.Protoss;

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 30, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 32, 20);
        probe.hp = 40;
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 33, 20);
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 34, 20);
        zealot.setEnemy();

        world(20, fakeOurs(nexus, cannon, probe, dragoon), fakeEnemies(zealot), () -> {
            if (A.now() != 12) return;

            AUnit worker = Select.ourWorkers().first();
            worker.move(zealot, Actions.MOVE_ATTACK, "SwarmMove");

            assertFalse(new WorkerDefenceStopFighting(worker).applies(),
                "a pursuing worker with a building at 2 and a Dragoon at 1 "
                + "is supported - the withdraw rule must not fire at home");
        });
    }

    @Override
    public Race initRace() {
        return Race.Protoss;
    }
}
