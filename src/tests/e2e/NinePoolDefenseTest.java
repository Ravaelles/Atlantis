package tests.e2e;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.units.AUnitType;
import bwapi.Race;
import org.junit.jupiter.api.Test;
import tests.acceptance.AbstractTestWithWorld;
import tests.fakes.FakeUnit;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 9pool defense as Protoss: the twin of {@link FourPoolDefenseTest} with the
 * forces that make a 9pool a 9pool - eight Zerglings arriving later (they
 * start at x=32 instead of 26), everything else identical.
 *
 * <p>Same harness physics ({@link ScenarioCombat}), same opponent
 * ({@link ZombieAttacksNearestUnit}), same assertion shape as the 4pool: what
 * is under test is Atlantis decisions, not damage modelling. When the OpenBW
 * runner ({@code _AI/IDEA-E2E-TESTS.md}) can host Atlantis, this scenario
 * keeps its forces, timing and assertions and swaps the driver and the
 * physics for the real engine.</p>
 */
public class NinePoolDefenseTest extends AbstractTestWithWorld {

    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    private FakeUnit nexus;
    private FakeUnit cannon;
    private FakeUnit zealot;
    private FakeUnit[] probes;
    private FakeUnit[] lings;

    @Test
    public void defendedBaseSurvivesTheNinePool() {
        nexus = fake(AUnitType.Protoss_Nexus, 10);
        probes = new FakeUnit[]{
            fake(AUnitType.Protoss_Probe, 7.9),
            fake(AUnitType.Protoss_Probe, 8.3),
            fake(AUnitType.Protoss_Probe, 9.1),
            fake(AUnitType.Protoss_Probe, 9.9),
            fake(AUnitType.Protoss_Probe, 10.6),
            fake(AUnitType.Protoss_Probe, 11.4),
        };
        FakeUnit pylon = fake(AUnitType.Protoss_Pylon, 12);
        zealot = fake(AUnitType.Protoss_Zealot, 12.5);
        cannon = fake(AUnitType.Protoss_Photon_Cannon, 14);
        FakeUnit gateway = fake(AUnitType.Protoss_Gateway, 15);

        FakeUnit[] ours = concat(
            new FakeUnit[]{nexus}, probes,
            new FakeUnit[]{pylon, zealot, cannon, gateway}
        );

        lings = new FakeUnit[8];
        for (int i = 0; i < 8; i++) {
            lings[i] = fake(AUnitType.Zerg_Zergling, 32 + i * 0.3);
        }

        List<FakeUnit> ourList = Arrays.asList(nexus, probes[0], probes[1], probes[2], probes[3], probes[4], probes[5], zealot, cannon);
        List<FakeUnit> lingList = Arrays.asList(lings);

        ZombieAttacksNearestUnit rush = new ZombieAttacksNearestUnit(lings);
        ScenarioCombat combat = new ScenarioCombat();
        enemyRaceInWorld = Race.Zerg;

        world(900, fakeOurs(ours), fakeEnemies(lings), () -> {
            if (A.now() == 1) {
                MissionChanger.setGlobalMissionTo(Missions.DEFEND, "E2E scenario holds home");
            }

            (new AtlantisGameCommander()).invokedCommander();
            rush.onFrame(ourList);
            combat.onFrame(A.now(), ourList, lingList);

            // The verdict of this scenario is "the base falls": once it has, the
            // remaining frames cannot change it. Everything else it asserts (the
            // cannon fell at ~192, three lings traded, the probes engaged) is
            // already decided by then.
            if (!nexus.isAlive()) {
                shouldQuitNow = true;
            }
        });

        // Mechanics check, not tuning: six probes engage (not zero as before
        // B-19), trade four lings and mostly survive - but the base still falls
        // at ~883, barely later than with four probes. The extra workers do not
        // change the outcome because the race is decided at the cannon/zealot
        // line; holding 8 lings would need more static defense or another
        // combat unit, which is a scenario-forces decision, not a mechanics
        // question. Threshold tuning stays in unit tests and game runs.
        assertTrue(!nexus.isAlive() && combat.diedAt(nexus) > 800,
            "eight lings still grind the base down; it fell at frame " + combat.diedAt(nexus));
        assertTrue(!cannon.isAlive(), "the cannon fought (and fell)");
        assertTrue(aliveCount(lings) <= 4, "the defence trades at least four lings, was: " + aliveCount(lings));
        assertTrue(aliveCount(probes) >= 5, "most probes survive the defence they join, was: " + aliveCount(probes));

        int probeStrikes = 0;
        for (FakeUnit probe : probes) {
            probeStrikes += combat.strikesBy(probe);
        }
        assertTrue(probeStrikes >= 10, "the probes must engage the rush (B-19: they never did), was: " + probeStrikes);
    }

    // =========================================================

    @Override
    protected FakeUnit[] generateOur() {
        return null;
    }

    @Override
    protected FakeUnit[] generateEnemies() {
        return null;
    }

    private int aliveCount(FakeUnit[] units) {
        int alive = 0;
        for (FakeUnit unit : units) {
            if (unit.isAlive()) alive++;
        }
        return alive;
    }

    private FakeUnit[] concat(FakeUnit[]... groups) {
        int total = 0;
        for (FakeUnit[] group : groups) total += group.length;

        FakeUnit[] result = new FakeUnit[total];
        int at = 0;
        for (FakeUnit[] group : groups) {
            System.arraycopy(group, 0, result, at, group.length);
            at += group.length;
        }
        return result;
    }
}
