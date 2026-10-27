package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.combat.strategy.DuelKite;
import atlantis.units.AUnit;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;

import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Labeled gate dataset for the learned hold-early gate (NEXT #185 S1).
 *
 * <p>Each row is one decision tick of a 1-dragoon-vs-1-zealot duel: raw
 * per-tick observables as features, {@link DuelKite#shouldYield} as the
 * label. Variety comes from spawn distances x enemy policies x spawn
 * seeds - the same three axes the Stage B scenarios vary, so a gate
 * that overfits one script fails the holdout instead of the suite.</p>
 *
 * <p>Features are deployment-readable quantities only (distances,
 * cooldown, motion, facing, ranges, hp) - never the hand gate's own
 * intermediates (effective range, race outcome), which the learner
 * exists to replace. Foe facing is oriented explicitly per policy
 * (chasers face us, kiters face away while retreating) so the facing
 * feature carries the same meaning it has live.</p>
 */
public final class HoldEarlyDataset {

    /** Feature count. Order is frozen: the net, the CSV and the gate agree on it. */
    public static final int FEATURES = 9;

    public static final int CHASE = 0;
    public static final int STAND = 1;
    public static final int KITEBACK = 2;
    public static final int HOLDSTILL = 3;

    public static final class Row {
        public final double[] x;
        public final double y;
        public final int policy;
        public final int seed;

        Row(double[] x, double y, int policy, int seed) {
            this.x = x;
            this.y = y;
            this.policy = policy;
            this.seed = seed;
        }
    }

    private HoldEarlyDataset() {
    }

    /**
     * The 9 deployment-readable features for one (unit, threat) pair.
     * Zeros and -1 (no threat range) on nulls - never throws.
     *
     * <p>Index 8 is the leaving conjunction (moving but not facing us):
     * the #186 commitment read as a first-class observable. It is a
     * measurement, not a gate intermediate - the net still learns every
     * threshold and weight around it. Without it the kite-back policy
     * is unlearnable from chasers alone (measured 0.838 vs 0.854
     * majority on the holdout).</p>
     */
    public static double[] features(AUnit unit, AUnit threat) {
        double[] x = new double[FEATURES];
        if (unit == null || threat == null || !unit.isAlive() || !threat.isAlive()) return x;

        try {
            x[0] = unit.distTo(threat);
            x[1] = unit.cooldown() / Math.max(1.0, unit.cooldownAbsolute());
            x[2] = threat.isMoving() ? 1 : 0;
            x[3] = unit.isOtherFacingThisUnit(threat) ? 1 : 0;
            x[4] = threat.maxSpeed() / 8.0;
            x[5] = unit.hp() / Math.max(1.0, unit.maxHp());
            x[6] = unit.shields() / Math.max(1.0, unit.maxShields());
            x[7] = unit.enemyWeaponRangeAgainstThisUnit(threat);
            x[8] = (x[2] > 0.5 && x[3] < 0.5) ? 1 : 0;
        } catch (Exception | Error ignored) {
        }
        return x;
    }

    /**
     * Holdout rule: the stand-and-trade policy is never trained on, and
     * neither is seed 2. The kite-back shape stays IN train on purpose:
     * a fully held-out kite-back is 100% one class (the hand rule never
     * yields to a runner) while its leaving conjunction is constant in
     * the rest of train - measured 0.647 vs 1.000 majority, unlearnable
     * without a single runner example, not a training failure. Runner
     * coverage in train is the S4 stimulant, on record in #185.
     */
    public static boolean isHoldout(int policy, int seed) {
        return policy == STAND || seed == 2;
    }

    /**
     * Runs the duel matrix and returns every decision tick as a row.
     * Deterministic: fixed spawns, scripted policies, no RNG anywhere
     * (cooldowns pinned, facing oriented, no sampling).
     */
    public static List<Row> generate() {
        List<Row> rows = new ArrayList<>();
        double[] dists = new double[] { 12, 15 };
        int[] policies = new int[] { CHASE, STAND, KITEBACK, HOLDSTILL };
        double[] jitters = new double[] { 0, 0.7, 1.4 };

        for (double dist : dists) {
            for (int policy : policies) {
                for (int seed = 0; seed < jitters.length; seed++) {
                    runDuel(rows, dist, jitters[seed], policy, seed);
                }
            }
        }
        return rows;
    }

    private static void runDuel(List<Row> rows, double dist, double jitter, int policy, int seed) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, 6, 6);
        dragoon.cooldown = 0;
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 6 + dist, 6 + jitter);
        zealot.setEnemy();
        zealot.cooldown = 0;

        List<FakeUnit> ours = new ArrayList<>();
        ours.add(dragoon);
        List<FakeUnit> foes = new ArrayList<>();
        foes.add(zealot);

        SimArena arena = new SimArena(20 * 32, 12 * 32);
        SimWorld world = new SimWorld(new FakeUnit[] { dragoon }, new FakeUnit[] { zealot }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 200; i++) {
                world.tick(() -> {
                    if (dragoon.hp <= 0 || zealot.hp <= 0) return;

                    rows.add(new Row(
                        features(dragoon, zealot),
                        DuelKite.shouldYield(dragoon, zealot) ? 1 : 0,
                        policy, seed));
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    drive(policy, foes, ours);
                    orient(foes.get(0), dragoon, policy);
                });
                if (dragoon.hp <= 0 || zealot.hp <= 0) break;
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    static void drive(int policy, List<FakeUnit> foes, List<FakeUnit> ours) {
        switch (policy) {
            case CHASE:
                ZombiePolicies.chaseNearest(foes, ours);
                break;
            case STAND:
                ZombiePolicies.standAndTrade(foes, ours);
                break;
            case KITEBACK:
                ZombiePolicies.kiteBack(foes, ours, 4.0);
                break;
            default:
                ZombiePolicies.holdStill(foes, ours);
                break;
        }
    }

    /** Chasers face us, kiters face away while retreating - the live meaning of facing. */
    static void orient(FakeUnit foe, FakeUnit dragoon, int policy) {
        if (foe == null || foe.hp <= 0 || dragoon.hp <= 0) return;

        double toward = Math.atan2(dragoon.y() - foe.y(), dragoon.x() - foe.x());
        if (policy == KITEBACK && "Move".equals(foe.lastCommand)) {
            foe.setAngle(toward + Math.PI);
        }
        else {
            foe.setAngle(toward);
        }
    }
}
