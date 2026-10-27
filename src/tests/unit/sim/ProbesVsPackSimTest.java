package tests.unit.sim;

import atlantis.units.AUnitType;
import atlantis.units.BaseUnderAttack;
import atlantis.units.workers.WorkerManager;
import bwapi.Race;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * B-19 in StarEngine: a packed early rush in the mineral line. Do the workers
 * help kill it, or does every one of them break off and run to its death?
 *
 * <p>
 * The shape this pins is the one measured in two independent engines. Wine
 * ({@code out/wine_runs/20261026_122342_ProtossBaseB_v_Zerg.scm_}, the game run
 * B-19 asked for): 14 Probes died to Zerglings and <b>13 of them carried the
 * tooltip {@code RunHellFar}</b> - each one had fought first
 * ({@code Attack:Zergling#.../WorkerDefenceFightCombatUnits}) and then, in its
 * last 10-30 frames, {@code WorkerDefenceRun} pulled it out of the swarm for a
 * cross-map run it could not win (Zergling 5.49 px/frame vs Probe 4.93). Same
 * story in this sim, which is why the sim is now the inner loop (CONVENTIONS
 * &sect;30): fight -&gt; {@code RunHellFar} -&gt; die running, with 7 of 8
 * Zerglings still alive at the end.
 * </p>
 *
 * <p>
 * The rule (B-19, "workers flee a packed rush they should help kill"): while
 * the base is under attack, a worker never abandons its own defence for a
 * long-range run. Fleeing is a field answer - at home it costs the swarm its
 * damage and still loses the race to the faster attacker, which is exactly
 * what both engines recorded.
 * </p>
 */
@Tag("combat")
public class ProbesVsPackSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    /**
     * Zerglings in the rush; measured red baseline killed only one of them.
     */
    private static final int PACK = 8;

    private static FakeUnit[] concatAll(FakeUnit nexus, FakeUnit cannon, List<FakeUnit> rest) {
        List<FakeUnit> all = new ArrayList<>();
        all.add(nexus);
        all.add(cannon);
        all.addAll(rest);

        return all.toArray(new FakeUnit[0]);
    }

    private static int countAlive(List<FakeUnit> units) {
        int n = 0;
        for (FakeUnit unit : units) if (unit.hp > 0) n++;

        return n;
    }

    // =========================================================
    // T1: the rule. No worker is sent on the long-range run while
    // its own base is being attacked - the swarm stays together.
    // =========================================================

    private static int countOccurrences(String text, String needle) {
        int count = 0;
        int from = 0;
        while (true) {
            int at = text.indexOf(needle, from);
            if (at < 0) return count;

            count++;
            from = at + needle.length();
        }
    }

    // =========================================================
    // T2: the point of staying. The standing swarm pays in Zergling
    // kills - fleeing measured 1 kill and 7 of 8 lings left standing.
    // =========================================================

    /**
     * The cannon auto-acquires like the engine does: nearest ling in range.
     */
    private static void scriptCannon(FakeUnit cannon, List<FakeUnit> lings) {
        if (cannon.hp <= 0) return;

        FakeUnit target = null;
        double best = Double.MAX_VALUE;
        for (FakeUnit ling : lings) {
            if (ling.hp <= 0) continue;
            double d = cannon.distTo(ling);
            if (d < best) {
                best = d;
                target = ling;
            }
        }
        if (target == null) return;

        cannon.lastCommand = "AttackUnit";
        cannon.target = target;
        cannon.targetPosition = null;
    }

    // =========================================================
    // T3: the scenario itself stays honest - the rush really is at
    // home, so "no long-range run" cannot be vacuous.
    // =========================================================

    /**
     * Scripted rush: every ling attacks the nearest living Probe.
     */
    private static void scriptLings(List<FakeUnit> lings, List<FakeUnit> probes) {
        for (FakeUnit ling : lings) {
            if (ling.hp <= 0) continue;
            FakeUnit target = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit probe : probes) {
                if (probe.hp <= 0) continue;
                double d = ling.distTo(probe);
                if (d < best) {
                    best = d;
                    target = probe;
                }
            }
            if (target == null) continue;
            ling.lastCommand = "AttackUnit";
            ling.target = target;
            ling.targetPosition = null;
        }
    }

    // =========================================================
    // Scenario: a Nexus, a Photon Cannon covering the mineral line, eight
    // Probes, and eight Zerglings charging in from the north-east. This is
    // the B-19 shape from the bug report: "nobody ever supported the cannon
    // while it died". The workers' job is to support the cannon, not to win
    // a duel alone - probes alone vs eight lings lose in real physics (the
    // no-support variant measured 0-2 kills), and no rule can change that.
    // =========================================================

    /**
     * The harness default is Terran; this is a Protoss mineral line (B-1 lesson).
     */
    @Override
    public Race initRace() {
        return Race.Protoss;
    }

    @Override
    public Race initEnemyRace() {
        return Race.Zerg;
    }

    @Test
    public void noWorkerAbandonsTheHomeFightForALongRangeRun() {
        Rush rush = packedRush();

        for (FakeUnit probe : rush.probes) {
            String history = probe.commandHistory().toString();
            assertFalse(history.contains("RunHellFar"), "a worker inside its own base under attack must not be pulled out"
                + " of the swarm for a cross-map run (B-19, rule"
                + " StandAndTradeAtHome): probe #" + probe.id()
                + " ordered " + history.replace("\n", " | "));
        }
    }

    @Test
    public void theStandingSwarmActuallyKillsThePack() {
        Rush rush = packedRush();

        int lingsKilled = PACK - rush.world.aliveEnemies();
        assertTrue(lingsKilled >= 6,
            "workers that stand and trade must take the rush with them"
                + " (B-19): killed " + lingsKilled + " of " + PACK
                + ", probes left " + countAlive(rush.probes)
                + " - the no-support flee baseline killed only 1,"
                + " cannon + fleeing swarm still bled the line");

        // Standing must not be silent: the swarm keeps issuing attacks after
        // the first wound, or "stay" is just "stand still and die".
        int attackOrders = 0;
        for (FakeUnit probe : rush.probes) {
            attackOrders += countOccurrences(probe.commandHistory().toString(), "Attack:");
        }
        assertTrue(attackOrders >= 8,
            "the swarm must keep engaging (B-19): only " + attackOrders
                + " attack orders across " + countAlive(rush.probes) + " living probes");
    }

    @Test
    public void theRushIsAtHomeNotInTheField() {
        Rush rush = packedRush();

        // Contact happened: the pack reached the line (a probe bled or
        // died) and the defence answered (lings died). Without contact
        // T1's "no run" would be vacuous and T2's kills unproven.
        boolean probeHurt = false;
        for (FakeUnit probe : rush.probes) {
            if (probe.hp < 40) {
                probeHurt = true;
                break;
            }
        }
        assertTrue(probeHurt,
            "the pack must actually reach the mineral line, else T1 proves nothing");
        assertTrue(PACK - rush.world.aliveEnemies() > 0,
            "the defence must have engaged the pack, else T2 proves nothing");
    }

    private Rush packedRush() {
        BaseUnderAttack.clearForTests();
        atlantis.util.log.EventLog.clearForTests();

        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 20, 20);
        FakeUnit cannon = fake(AUnitType.Protoss_Photon_Cannon, 22, 22);
        List<FakeUnit> probes = new ArrayList<>();
        for (double[] spot : new double[][]{
            {19, 23}, {20, 23}, {21, 23}, {22, 23},
            {18, 23}, {23, 23}, {20, 24}, {21, 24}}) {
            FakeUnit probe = fake(AUnitType.Protoss_Probe, spot[0], spot[1]);
            probe.hp = 40;   // real Probe: 20 hit points + 20 shields (hp() reads both)
            probes.add(probe);
        }

        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][]{
            {36, 20}, {37, 21}, {36, 19}, {38, 20},
            {37, 20}, {36, 22}, {38, 22}, {38, 18}}) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            concatAll(nexus, cannon, probes), lings.toArray(new FakeUnit[0]), arena);
        try {
            for (int i = 0; i < 300; i++) {
                world.tick(() -> {
                    for (FakeUnit probe : probes) {
                        if (probe.hp <= 0) continue;
                        new WorkerManager(probe).invokeFrom(new Object());
                    }
                    scriptCannon(cannon, lings);
                    scriptLings(lings, probes);
                });
            }
        } catch (RuntimeException e) {
            world.close();
            throw e;
        }

        return new Rush(nexus, probes, lings, world);
    }

    private static final class Rush {
        final FakeUnit nexus;
        final List<FakeUnit> probes;
        final List<FakeUnit> lings;
        final SimWorld world;

        Rush(FakeUnit nexus, List<FakeUnit> probes, List<FakeUnit> lings, SimWorld world) {
            this.nexus = nexus;
            this.probes = probes;
            this.lings = lings;
            this.world = world;
        }
    }
}
