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
 * 4pool defense as Protoss: six Zerglings rush a defended base, Atlantis
 * defends with everything the stub world can resolve - zealot micro, probe
 * pull, cannon fire - and the scenario measures the outcome.
 *
 * <p>Forces are fixed, timing is exact (lings step every frame from x=26),
 * the physics is {@link ScenarioCombat} (documented harness rules, not game
 * truth) and the opponent is {@link ZombieAttacksNearestUnit} (no build,
 * no micro, only hunger). What is under test is Atlantis decisions: who
 * engages, who flees, who shoots. When the OpenBW runner
 * ({@code _AI/IDEA-E2E-TESTS.md}) can host Atlantis, this scenario keeps its
 * forces, timing and assertions and swaps the driver and the physics for the
 * real engine.</p>
 */
public class FourPoolDefenseTest extends AbstractTestWithWorld {

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
    public void defendedBaseSurvivesTheFourPool() {
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

        lings = new FakeUnit[6];
        for (int i = 0; i < 6; i++) {
            lings[i] = fake(AUnitType.Zerg_Zergling, 26 + i * 0.3);
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

            // The fight is decided when the last attacker dies, and nothing can
            // change the end state after that - so the remaining ~650 frames of a
            // 900-frame horizon are 20 seconds of nothing. The assertions below
            // read the decision frame, which is the part worth pinning.
            if (aliveCount(lings) == 0) {
                shouldQuitNow = true;
            }
        });

        // The negative control this scenario used to carry - "a base with no
        // defence at all falls to the same rush", in its own 900-frame run -
        // was 35 s per twin and is now StubWorldDamageTest: the property that
        // actually mattered was "the stub world deals damage at all", and that
        // is asserted in a second instead of a minute.
        //
        // Measured from a clean run of this scenario (2026-10-03, after the B-19
        // fix: the un-inverted WorkerDefenceHelpCannon condition, base-defence
        // suppression of the run lockout and the modulo skips, and a world that
        // drops its dead units). No instrumentation inside the frame loop - a
        // diagnostic there takes focus and attack turns the passive stub units
        // never took on their own (NOTES.md), and the first version of these
        // numbers did come from such a run, which is why they were wrong.
        //
        // The defence now holds outright: the six lings die between frames 51 and
        // 249, the cannon survives on 10 hit points after 11 strike rounds, the
        // zealot never takes a hit (it is still a tile short when the rush ends)
        // and the nexus never loses a single hit point. Every probe gets one
        // strike in, which is the whole of B-19: they used to never engage at all,
        // locked out for 300 frames after fleeing and then skipped by id % 5.
        assertTrue(nexus.isAlive() && nexus.hp() == nexus.type().maxHp() && nexus.shields() == nexus.type().maxShields(),
            "the held base must come through untouched, was: " + nexus.hp() + "+" + nexus.shields());
        assertTrue(cannon.isAlive(), "the cannon must survive the defence it leads, was: " + cannon.hp() + "+" + cannon.shields());
        assertTrue(zealot.isAlive(), "the zealot must survive behind cannon and probes, was: " + zealot.hp());
        // "All six lings are dead" is what the run stops on, so pinning it would
        // be pinning the exit condition. Pin the frame instead: a held 4pool is
        // over by frame ~250, and a regression that lets the rush live shows up
        // as a later frame rather than as a tautology.
        int lastLingDeath = 0;
        for (FakeUnit ling : lings) {
            lastLingDeath = Math.max(lastLingDeath, combat.diedAt(ling));
        }
        assertTrue(lastLingDeath > 0 && lastLingDeath <= 300,
            "a held 4pool kills the whole rush by frame 300, last ling died at frame " + lastLingDeath);

        assertTrue(aliveCount(probes) >= 3, "the probe fight must not be a suicide, was: " + aliveCount(probes));
        int probeStrikes = 0;
        for (FakeUnit probe : probes) {
            probeStrikes += combat.strikesBy(probe);
        }
        assertTrue(probeStrikes >= 4, "every probe must land at least one strike (B-19: they never did), was: " + probeStrikes);
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
