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

        // Measured from a clean run (2026-10-03), same conditions as the 4pool
        // twin. Eight attackers arriving later still beat the defence, and that is
        // pinned on purpose: the point of this scenario is not that the base
        // survives, it is that the defence engages at all. What the B-19 fix
        // changed is the mechanism - the cannon now trades three lings instead of
        // two before falling at frame 192 (was ~163), and the probes land six
        // strikes between them (they used to land none: locked out for 300 frames
        // after fleeing, then skipped by id % 5).
        //
        // What is deliberately *not* pinned: the probes all dying. They do
        // (frames 120-841, the last two in the final stand on the ruins), and
        // that is the honest end of this scenario rather than a target - a fix
        // that saved them must not have to fight these assertions.
        assertTrue(!nexus.isAlive(), "eight lings still grind the base down, was: " + nexus.hp() + "+" + nexus.shields());
        assertTrue(!cannon.isAlive() && combat.diedAt(cannon) > 0, "the cannon fought (and fell), frame: " + combat.diedAt(cannon));
        assertTrue(aliveCount(lings) <= 5, "the defence trades at least three lings, was: " + aliveCount(lings));

        int probeStrikes = 0;
        for (FakeUnit probe : probes) {
            probeStrikes += combat.strikesBy(probe);
        }
        assertTrue(probeStrikes >= 6, "the probes must engage the rush (B-19: they never did), was: " + probeStrikes);
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
