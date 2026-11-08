package tests.unit;

import atlantis.units.AUnitType;
import atlantis.units.workers.defence.run.WorkerDefenceRun;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.acceptance.WorldStubForTests;
import tests.fakes.FakeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;
import static tests.unit.AbstractTestWithUnits.fakeEnemies;
import static tests.unit.AbstractTestWithUnits.fakeOurs;

/**
 * Fleeing workers go home at two or fewer bases (owner 2026-11-09): no
 * cross-map region run that strands a Probe 40 tiles out past the
 * natural. The worker is assigned straight to a random mineral near
 * the main - the gather order routes it home fastest and is
 * transparent to the fight managers (gathering units are skipped).
 */
@Tag("production")
public class WorkerFleeToMainTest extends WorldStubForTests {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    private static FakeUnit[] lingsAt(int tx, int ty) {
        return new FakeUnit[]{
            fake(AUnitType.Zerg_Zergling, tx, ty).setEnemy(),
            fake(AUnitType.Zerg_Zergling, tx + 1, ty).setEnemy(),
            fake(AUnitType.Zerg_Zergling, tx, ty + 1).setEnemy(),
        };
    }

    @Test
    public void fleeingWorkerGathersNearMainAtTwoBases() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 60, 20);
        probe.hp = 20;
        FakeUnit[] lings = lingsAt(60, 20);

        neutralInWorld = new FakeUnit[]{
            fake(AUnitType.Resource_Mineral_Field, 23, 20).setNeutral()};

        world(5, fakeOurs(main, natural, probe),
            fakeEnemies(lings[0], lings[1], lings[2]), () -> {
                if (atlantis.game.A.now() != 2) return;

                new WorkerDefenceRun(probe).handle();

                assertTrue(probe.isGatheringMinerals(),
                    "flee means gather, not a region run: " + probe.tooltip());
                assertEquals("RunToMainMinerals", probe.tooltip(),
                    "the twin names the new rule");
            });
    }

    @Test
    public void threeBasesKeepTheRegionRun() {
        FakeUnit main = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit natural = fake(AUnitType.Protoss_Nexus, 38, 20);
        FakeUnit third = fake(AUnitType.Protoss_Nexus, 20, 60);
        FakeUnit probe = fake(AUnitType.Protoss_Probe, 60, 20);
        probe.hp = 20;
        FakeUnit[] lings = lingsAt(60, 20);

        neutralInWorld = new FakeUnit[]{
            fake(AUnitType.Resource_Mineral_Field, 23, 20).setNeutral()};

        world(5, fakeOurs(main, natural, third, probe),
            fakeEnemies(lings[0], lings[1], lings[2]), () -> {
                if (atlantis.game.A.now() != 2) return;

                new WorkerDefenceRun(probe).handle();

                assertFalse(probe.isGatheringMinerals(),
                    "three bases: the field keeps the old region run");
            });
    }
}
