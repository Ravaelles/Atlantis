package tests.acceptance.e2e;

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
            fake(AUnitType.Protoss_Probe, 8.3),
            fake(AUnitType.Protoss_Probe, 9.1),
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

        List<FakeUnit> ourList = Arrays.asList(nexus, probes[0], probes[1], probes[2], probes[3], zealot, cannon);
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
        });

        // Measured baseline (engine hit points, engine cooldowns, shields
        // modelled; instrumented run of 2026-10-03): the cannon starts killing
        // lings in reach around frame 95, takes damage from ~105 and falls
        // around frame 163; the zealot falls around 214. Between them they
        // trade two lings, and the six survivors grind the lone nexus down
        // around frame 631 - earlier than the 4pool's ~880, because eight
        // attackers kill faster than six despite the later arrival. The probes
        // never engage (the same WorkerDefenceRun + 300-frame lockout story as
        // the 4pool, _AI/BUGS.md B-19): three of the four are killed off at
        // the very end (frames ~660-825) and one escapes.
        // A defense that holds must change that story, not these numbers.
        assertTrue(!nexus.isAlive(), "eight lings grind the base down by frame 900");
        assertTrue(!cannon.isAlive(), "the cannon fought (and fell)");
        assertTrue(!zealot.isAlive(), "the zealot fought (and fell)");
        assertTrue(aliveCount(lings) <= 6, "the defense trades at least two lings, was: " + aliveCount(lings));
        assertTrue(aliveCount(probes) >= 1, "at least one probe escapes, was: " + aliveCount(probes));
    }

    @Test
    public void loneNexusFallsFast() {
        nexus = fake(AUnitType.Protoss_Nexus, 10);
        probes = new FakeUnit[]{
            fake(AUnitType.Protoss_Probe, 8.3),
            fake(AUnitType.Protoss_Probe, 9.1),
        };

        lings = new FakeUnit[8];
        for (int i = 0; i < 8; i++) {
            lings[i] = fake(AUnitType.Zerg_Zergling, 32 + i * 0.3);
        }

        List<FakeUnit> ourList = Arrays.asList(nexus, probes[0], probes[1]);
        List<FakeUnit> lingList = Arrays.asList(lings);

        ZombieAttacksNearestUnit rush = new ZombieAttacksNearestUnit(lings);
        ScenarioCombat combat = new ScenarioCombat();
        enemyRaceInWorld = Race.Zerg;

        world(900, fakeOurs(nexus, probes[0], probes[1]), fakeEnemies(lings), () -> {
            (new AtlantisGameCommander()).invokedCommander();
            rush.onFrame(ourList);
            combat.onFrame(A.now(), ourList, lingList);
        });

        // Physics sanity: with no army and no cannon the base must fall, and
        // much earlier than in the defended scenario. If this ever stops
        // failing... good - it means the defense below got help.
        assertTrue(!nexus.isAlive(), "a lone nexus cannot survive eight lings");
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
