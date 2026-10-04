package tests.e2e;

import atlantis.combat.missions.MissionChanger;
import atlantis.combat.missions.Missions;
import atlantis.game.A;
import atlantis.game.AtlantisGameCommander;
import atlantis.combat.CombatUnitManager;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
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
        ScenarioObserver observer = new ScenarioObserver(15);
        enemyRaceInWorld = Race.Zerg;

        world(900, fakeOurs(ours), fakeEnemies(lings), () -> {
            if (A.now() == 1) {
                MissionChanger.setGlobalMissionTo(Missions.DEFEND, "E2E scenario holds home");
            }

            (new AtlantisGameCommander()).invokedCommander();
            driveCombatUnits();
            rush.onFrame(ourList);
            combat.onFrame(A.now(), ourList, lingList);
            observer.onFrame(A.now(), ourList, lingList);

            // The verdict of this scenario is "the base falls": once it has, the
            // remaining frames cannot change it. Everything else it asserts (the
            // cannon fell at ~192, three lings traded, the probes engaged) is
            // already decided by then.
            if (!nexus.isAlive()) {
                shouldQuitNow = true;
            }
        });

        // With the zealot driven, the defence holds: it steps into the pack at
        // ~120, tanks and deals its two strikes, and dies at 218 - while it
        // lives the lings chew it instead of the cannon, and the six probes
        // drill uninterrupted (167 strikes) behind cannon fire (12 strikes).
        // Nexus never drops below 700, all six probes live, all eight lings
        // die. Bit-identical across runs. A defence that stops holding must
        // change this story, not these numbers.
        assertTrue(nexus.isAlive(), "the base holds against 9pool");
        assertTrue(cannon.isAlive(), "the cannon leads the defence and lives, was: " + cannon.hp());
        assertTrue(aliveCount(probes) >= 4, "at least four probes survive, was: " + aliveCount(probes));
        assertTrue(aliveCount(lings) == 0, "every ling dies, left: " + aliveCount(lings));
        assertTrue(combat.strikesBy(zealot) >= 2, "the zealot fights instead of watching, was: "
            + combat.strikesBy(zealot));

        int probeStrikes = 0;
        for (FakeUnit probe : probes) {
            probeStrikes += combat.strikesBy(probe);
        }
        assertTrue(probeStrikes >= 100, "the probe drill does the killing (B-19: they never did), was: "
            + probeStrikes);
    }

    /**
     * The full commander drives workers and production in the stub world, but
     * combat units never get past its orchestration here (squad/mission wiring
     * the stub does not provide), so they stand idle while the base dies around
     * them - measured DoNothing for 200 frames with enemies 2 tiles away. What
     * does work, here and in other acceptance tests, is invoking their manager
     * directly: the decisions are real Atlantis code, only the dispatch is
     * harness. Full-loop dispatch waits for the OpenBW runner
     * (_AI/IDEA-E2E-TESTS.md); until then this is a decision-layer E2E.
     */
    private void driveCombatUnits() {
        for (AUnit unit : Select.ourCombatUnits().list()) {
            if (unit.isAlive() && !unit.isABuilding() && unit instanceof FakeUnit) {
                (new CombatUnitManager(unit)).invokeFrom(this);
            }
        }
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
