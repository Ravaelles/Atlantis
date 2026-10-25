package tests.unit.sim;

import atlantis.combat.BattleCommander;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Zealot withdraw sim proof (NEXT #178): a wounded zealot on a spent weapon
 * steps back instead of standing through the cooldown against faster
 * attackers, and keeps trading while yielding. Mirrors the T1 dragoon
 * shape - displacement plus move steps plus shots, never hold-and-die.
 * Survival is not asserted (2 lings can still eat one zealot); the
 * contract is yield-while-trading, the same bar the dragoon pins meet.
 */
@Tag("combat")
public class ZealotWithdrawSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    @Test
    public void woundedZealotYieldsWhileTrading() {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, 30, 20);
        zealot.hp = 80;
        zealot.shields = 60;
        zealot.cooldown = 15;

        List<FakeUnit> lings = new ArrayList<>();
        for (double[] spot : new double[][] { { 32, 19 }, { 32, 21 } }) {
            FakeUnit ling = fake(AUnitType.Zerg_Zergling, spot[0], spot[1]);
            ling.setEnemy();
            lings.add(ling);
        }

        SimArena arena = new SimArena(60 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { zealot }, lings.toArray(new FakeUnit[0]), arena);
        try {
            double startX = zealot.position.x();
            double startY = zealot.position.y();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();

            for (int i = 0; i < 250; i++) {
                world.tick(() -> {
                    BattleCommander.act(
                        zealot, FightOrFlight.Tactic.FIGHT, null, zealot);
                    scriptLings(lings, zealot);
                });
                if (zealot.hp <= 0) break;
            }

            double fledPx = Math.hypot(
                zealot.position.x() - startX, zealot.position.y() - startY);
            assertTrue(fledPx > TILE,
                "wounded zealot must yield ground from the pocket, fled only " + fledPx + "px");
            assertTrue(hasOrder(ordersOf(zealot), zealot.id(), "move"),
                "withdraw must issue move steps, not stand-and-bleed: " + ordersOf(zealot));
            assertTrue(world.shotsFired() > 0,
                "a withdraw that never shoots is just running - trade while yielding");
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static boolean hasOrder(List<FakeOrderSink.Record> orders, int unitId, String operation) {
        for (FakeOrderSink.Record record : orders) {
            if (record.actorId == unitId && record.operation.equals(operation)) return true;
        }
        return false;
    }

    /** Scripted pressers: nearest live zealot, move into range, then trade. */
    private static void scriptLings(List<FakeUnit> lings, FakeUnit... zealots) {
        for (FakeUnit ling : lings) {
            if (ling.hp <= 0) continue;
            FakeUnit nearest = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit zealot : zealots) {
                if (zealot.hp <= 0) continue;
                double dist = ling.distTo(zealot);
                if (dist < best) {
                    best = dist;
                    nearest = zealot;
                }
            }
            if (nearest == null) {
                ling.lastCommand = "Stop";
                continue;
            }
            if (best <= SimWorld.reachTiles(ling, nearest)) {
                ling.lastCommand = "AttackUnit";
                ling.target = nearest;
                ling.targetPosition = nearest.position;
            }
            else {
                ling.lastCommand = "Move";
                ling.target = nearest;
                ling.targetPosition = nearest.position;
            }
        }
    }
}
