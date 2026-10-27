package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.combat.Behavior;
import atlantis.combat.strategy.DuelKite;
import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnitType;
import atlantis.units.actions.Actions;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * S1 vehicle proof (NEXT #185): the hold-early gate cloned from
 * {@link DuelKite#shouldYield} on fake-map duels.
 *
 * <p>Bar, all three or S1 fails: (1) training loss goes down, (2)
 * held-out agreement (unseen policy + unseen seed) beats the
 * majority-class baseline by margin, (3) duel wins are preserved when
 * the net replaces the hand gate 1:1 downstream.</p>
 */
@Tag("combat")
public class LearnedHoldEarlyGateTest extends AbstractTestWithUnits {

    private static Fixture fixture;

    private static final class Fixture {
        SimMLP net;
        LearnedHoldEarlyGate gate;
        double initLoss;
        double finalLoss;
        double agreement;
        double majority;
        int holdoutRows;
    }

    private static synchronized Fixture fixture() {
        if (fixture != null) return fixture;

        fixture = new Fixture();

        List<HoldEarlyDataset.Row> rows = HoldEarlyDataset.generate();
        List<double[]> trainX = new ArrayList<>();
        List<Double> trainY = new ArrayList<>();
        List<double[]> testX = new ArrayList<>();
        List<Double> testY = new ArrayList<>();
        List<Integer> testPolicy = new ArrayList<>();
        for (HoldEarlyDataset.Row r : rows) {
            if (HoldEarlyDataset.isHoldout(r.policy, r.seed)) {
                testX.add(r.x);
                testY.add(r.y);
                testPolicy.add(r.policy);
            }
            else {
                trainX.add(r.x);
                trainY.add(r.y);
            }
        }

        double[][] tx = trainX.toArray(new double[0][]);
        double[] ty = trainY.stream().mapToDouble(Double::doubleValue).toArray();
        double[][] ex = testX.toArray(new double[0][]);
        double[] ey = testY.stream().mapToDouble(Double::doubleValue).toArray();

        SimMLP net = new SimMLP(7, HoldEarlyDataset.FEATURES, 12, 1);
        fixture.initLoss = net.meanLoss(tx, ty);
        fixture.finalLoss = net.train(tx, ty, 300, 0.05);

        int agree = 0;
        int ones = 0;
        StringBuilder per = new StringBuilder();
        int[] policies = new int[] {
            HoldEarlyDataset.CHASE, HoldEarlyDataset.STAND,
            HoldEarlyDataset.KITEBACK, HoldEarlyDataset.HOLDSTILL };
        for (int p : policies) {
            int pa = 0;
            int pn = 0;
            int po = 0;
            for (int i = 0; i < ex.length; i++) {
                if (testPolicy.get(i) != p) continue;
                pn++;
                if (ey[i] > 0.5) po++;
                if ((net.predictProb(ex[i]) >= 0.5) == (ey[i] > 0.5)) pa++;
            }
            per.append(" p").append(p).append("=").append(String.format("%.3f", pa / (double) pn))
                .append("(maj").append(String.format("%.3f", Math.max(po, pn - po) / (double) pn))
                .append("/n").append(pn).append(")");
        }
        for (int i = 0; i < ex.length; i++) {
            if (ey[i] > 0.5) ones++;
            if ((net.predictProb(ex[i]) >= 0.5) == (ey[i] > 0.5)) agree++;
        }
        fixture.agreement = agree / (double) ex.length;
        fixture.majority = Math.max(ones, ex.length - ones) / (double) ex.length;
        fixture.holdoutRows = ex.length;
        fixture.net = net;
        fixture.gate = new LearnedHoldEarlyGate(net);

        System.out.println("HOLD-EARLY-S1 rows=" + rows.size()
            + " train=" + tx.length + " holdout=" + ex.length
            + " initLoss=" + String.format("%.4f", fixture.initLoss)
            + " finalLoss=" + String.format("%.4f", fixture.finalLoss)
            + " agreement=" + String.format("%.3f", fixture.agreement)
            + " majority=" + String.format("%.3f", fixture.majority)
            + " per-policy:" + per);

        return fixture;
    }

    @Test
    public void lossGoesDown() {
        Fixture f = fixture();
        assertTrue(f.finalLoss < f.initLoss * 0.5,
            "training must clearly descend: " + f.initLoss + " -> " + f.finalLoss);
    }

    @Test
    public void beatsMajorityOnHoldout() {
        Fixture f = fixture();
        assertTrue(f.holdoutRows >= 200,
            "the holdout must be a real slice, not a token: " + f.holdoutRows);
        assertTrue(f.agreement >= f.majority + 0.10,
            "held-out agreement " + f.agreement + " must beat majority "
                + f.majority + " by margin");
    }

    @Test
    public void downstreamDuelWinsPreserved() {
        Fixture f = fixture();
        int[][] scenarios = new int[][] {
            { 12, HoldEarlyDataset.CHASE }, { 15, HoldEarlyDataset.CHASE },
            { 12, HoldEarlyDataset.KITEBACK }, { 15, HoldEarlyDataset.KITEBACK },
            { 12, HoldEarlyDataset.HOLDSTILL }, { 15, HoldEarlyDataset.HOLDSTILL },
        };
        for (int[] s : scenarios) {
            boolean hand = runDuel(s[0], 2.1, s[1], f.gate, false);
            assertTrue(hand, "hand gate must win dist=" + s[0] + " policy=" + s[1]
                + " or the comparison is vacuous");
            boolean net = runDuel(s[0], 2.1, s[1], f.gate, true);
            assertTrue(net, "net gate must preserve the win dist=" + s[0] + " policy=" + s[1]);
        }
    }

    private static boolean runDuel(
        double dist, double jitter, int policy, LearnedHoldEarlyGate gate, boolean useNet
    ) {
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
        final Integer[] lastYield = new Integer[] { null };
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 600; i++) {
                world.tick(() -> {
                    if (dragoon.hp <= 0 || zealot.hp <= 0) return;

                    boolean yield = useNet
                        ? gate.shouldYield(dragoon, zealot)
                        : DuelKite.shouldYield(dragoon, zealot);
                    if (yield && DuelKite.yieldNow(A.now(), lastYield[0])) {
                        APosition dest = DuelKite.yieldStep(dragoon, zealot);
                        if (dest != null) {
                            dragoon.move(dest, Actions.MOVE_AVOID, Behavior.DUEL_YIELD.tag());
                            lastYield[0] = A.now();
                        }
                        else {
                            dragoon.attackUnit(zealot);
                        }
                    }
                    else {
                        dragoon.attackUnit(zealot);
                    }
                    HoldEarlyDataset.drive(policy, foes, ours);
                    HoldEarlyDataset.orient(foes.get(0), dragoon, policy);
                });
                if (zealot.hp <= 0 || dragoon.hp <= 0) break;
            }
            return zealot.hp <= 0;
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }
}
