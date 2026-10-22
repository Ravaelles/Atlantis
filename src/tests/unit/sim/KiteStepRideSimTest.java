package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * A running kite step must not be cancelled by a fresh attack order
 * (owner: kiting has absolute precedence). Live Dragoon#96 showed the
 * shape: a stepped-aside row every ~10 frames, zero displacement, 20
 * hits in - retargeting re-issued attackUnit every frame and each
 * issue cancelled the step the kite had just ordered. The re-arm
 * respects the step's own cadence clock now: while a step rides,
 * no new attack order issues; when the step finds nothing the clock
 * goes stale and cornered units still trade.
 */
@Tag("combat")
public class KiteStepRideSimTest extends AbstractTestWithUnits {
    private static final int TILE = 32;

    @Test
    public void rearmDoesNotCancelARidingStep() {
        // Swamped (3+ melee in 2 tiles): the riding step keeps riding,
        // no fresh attack order cancels it - trading blows here loses
        // the DPS race, so the guns stay quiet until the step lands.
        FakeUnit dragoon = mk(30, 20);
        dragoon.cooldown = 20;
        FakeUnit foeA = me(30.4, 20);
        FakeUnit foeB = me(29.6, 20);
        FakeUnit foeC = me(30, 20.5);
        FakeUnit[] foes = new FakeUnit[] { foeA, foeB, foeC };
        List<FakeUnit> ours = new ArrayList<FakeUnit>();
        ours.add(dragoon);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 3; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptFoes(foes, ours);
                });
            }

            assertEquals(1, movesOf(dragoon),
                "one kite step issued, cadence holds the rest: " + ordersOf(dragoon));
            assertEquals(0, attacksOf(dragoon),
                "swamped: no attack order cancels the riding step: " + ordersOf(dragoon));
        }
        finally {
            Alpha.get().removeUnit(dragoon);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void rearmFiresAgainstASingleChewer() {
        // One chewer is a DPS race the dragoon wins standing and
        // shooting - the guard only covers the swamping pack.
        FakeUnit dragoon = mk(30, 20);
        FakeUnit foe = me(30.4, 20);
        List<FakeUnit> ours = new ArrayList<FakeUnit>();
        ours.add(dragoon);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), new FakeUnit[] { foe }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 3; i++) {
                world.tick(() -> {
                    if (dragoon.hp > 0) {
                        BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptFoes(new FakeUnit[] { foe }, ours);
                });
            }

            assertTrue(attacksOf(dragoon) >= 1,
                "lone chewer gets shot, the step still rides between shots: "
                    + ordersOf(dragoon));
        }
        finally {
            Alpha.get().removeUnit(dragoon);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    @Test
    public void rearmStillFiresWithoutARunningStep() {
        // The guard keys on the step clock, not on kiting in general: a
        // dragoon that never stepped re-arms normally on a ready weapon.
        FakeUnit dragoon = mk(30, 20);
        FakeUnit foe = me(33.1, 20);
        List<FakeUnit> ours = new ArrayList<FakeUnit>();
        ours.add(dragoon);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), new FakeUnit[] { foe }, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            world.tick(() -> {
                if (dragoon.hp > 0) {
                    BattleCommander.act(dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                }
                scriptFoes(new FakeUnit[] { foe }, ours);
            });

            assertEquals(1, attacksOf(dragoon),
                "no step riding: the ready weapon still re-arms: " + ordersOf(dragoon));
        }
        finally {
            Alpha.get().removeUnit(dragoon);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private static String ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders().toString();
    }

    private static int movesOf(FakeUnit unit) {
        int moves = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) unit.orderSink()).orders()) {
            if (r.actorId == unit.id() && r.operation.equals("move")) moves++;
        }
        return moves;
    }

    private static int attacksOf(FakeUnit unit) {
        int attacks = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) unit.orderSink()).orders()) {
            if (r.actorId == unit.id() && r.operation.equals("attackUnit")) attacks++;
        }
        return attacks;
    }

    private static FakeUnit mk(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 180;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit me(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.setEnemy();
        z.hp = 100;
        z.shields = 0;
        z.cooldown = 0;

        return z;
    }

    private static void scriptFoes(FakeUnit[] foes, List<FakeUnit> ours) {
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            FakeUnit n = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit u : ours) {
                if (u.hp <= 0) continue;
                double d = f.distTo(u);
                if (d < best) {
                    best = d;
                    n = u;
                }
            }
            if (n == null) {
                f.lastCommand = "Stop";
                continue;
            }
            f.lastCommand = best <= SimWorld.reachTiles(f, n) ? "AttackUnit" : "Move";
            f.target = n;
            f.targetPosition = n.position;
        }
    }
}
