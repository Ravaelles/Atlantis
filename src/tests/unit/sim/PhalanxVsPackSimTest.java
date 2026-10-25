package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Phalanx vs pack on a fast behavior sim (owner 2026-10-26): 2 dragoons +
 * 2 zealots against 5 enemy zealots, empty map, 600 ticks. The StarCraft
 * vehicle is gone; this in-JVM sim is the fast loop now - same job,
 * milliseconds instead of minutes.
 *
 * <p>This is a tripwire test, not a quality bar: it fails only on total
 * breakage of the combat loop (dragoons that stand while chewed, guns
 * that never fire). The contract it pins: melee never reaches hitting
 * range of a dragoon (0.5 tiles), no dragoon drops below 150 hp, and
 * every dragoon issues attack orders instead of only marching.</p>
 */
@Tag("combat")
public class PhalanxVsPackSimTest extends AbstractTestWithUnits {
    private static final int TILE = 32;

    /** Zergling/zealot claws reach 15 px: inside this the dragoon is being hit. */
    private static final double MELEE_REACH_TILES = 0.5;

    @Test
    public void dragoonsKiteAndShootInsteadOfStanding() {
        final int[] minHp = new int[] { Integer.MAX_VALUE };
        final double[] minDist = new double[] { Double.MAX_VALUE };

        FakeUnit[] dragoons = new FakeUnit[] {
            mk(28, 20), mk(32, 20),
        };
        FakeUnit[] zealots = new FakeUnit[] {
            mkz(29, 19), mkz(31, 19),
        };
        FakeUnit[] foes = new FakeUnit[] {
            me(38, 19), me(38, 20), me(38, 21), me(38, 18), me(38, 22),
        };
        List<FakeUnit> ours = new ArrayList<FakeUnit>();
        for (FakeUnit u : dragoons) ours.add(u);
        for (FakeUnit u : zealots) ours.add(u);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 600; i++) {
                world.tick(() -> {
                    atlantis.map.position.APosition anchor =
                        atlantis.map.position.APosition.create(60, 20);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, dragoons[0]);
                    }
                    scriptFoes(foes, ours);
                });
                for (FakeUnit d : dragoons) {
                    // Dead counts as zero - a corpse must trip the HP floor,
                    // not slip past it through a null position.
                    int cur = d.hp <= 0 ? 0 : d.hp + d.shields;
                    if (cur < minHp[0]) minHp[0] = cur;
                    if (d.hp <= 0 || d.position() == null) continue;
                    for (FakeUnit f : foes) {
                        if (f.hp <= 0 || f.position() == null) continue;
                        double dist = d.distTo(f);
                        if (dist < minDist[0]) minDist[0] = dist;
                    }
                }
                boolean allDead = true;
                for (FakeUnit u : ours) if (u.hp > 0) allDead = false;
                if (allDead) break;
            }

            assertTrue(minDist[0] > MELEE_REACH_TILES,
                "melee never reaches hitting range of a dragoon, closest was "
                    + minDist[0]);
            assertTrue(minHp[0] >= 150,
                "no dragoon drops below 150 hp, lowest was " + minHp[0]);
            for (FakeUnit d : dragoons) {
                assertTrue(attacksOf(d) >= 1,
                    "dragoon " + d.id() + " issues attack orders instead of only marching");
            }
        }
        finally {
            for (FakeUnit u : ours) Alpha.get().removeUnit(u);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private static int attacksOf(FakeUnit dragoon) {
        int attacks = 0;
        for (FakeOrderSink.Record r : ((FakeOrderSink) dragoon.orderSink()).orders()) {
            if (r.actorId == dragoon.id() && r.operation.equals("attackUnit")) attacks++;
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

    private static FakeUnit mkz(double tx, double ty) {
        return fake(AUnitType.Protoss_Zealot, tx, ty);
    }

    private static FakeUnit me(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.setEnemy();

        return z;
    }

    /** Scripted pressers: nearest live unit, move into range, then trade. */
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
