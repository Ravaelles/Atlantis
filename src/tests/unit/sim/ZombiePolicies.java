package tests.unit.sim;

import tests.fakes.FakeUnit;

import java.util.List;

/**
 * Scripted enemy policies for behavior scenarios (NEXT #185 Stage B:
 * enemy POLICY variety is mandatory, so gates cannot overfit one
 * script). Deterministic, no randomness - scenarios stay reproducible.
 *
 * <p>Movement and combat follow the sim's own rules: an "AttackUnit"
 * zombie walks into reach and trades once there, a "Move" zombie
 * repositions without shooting that tick, anything else brakes to a
 * stop. Policies differ in WHO they answer, not in physics.</p>
 */
public final class ZombiePolicies {

    private ZombiePolicies() {
    }

    /**
     * Relentless: every zombie re-targets the nearest live unit each tick.
     */
    public static void chaseNearest(List<FakeUnit> foes, List<FakeUnit> ours) {
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            FakeUnit n = nearest(f, ours);
            if (n == null) {
                f.lastCommand = "Stop";
                continue;
            }
            f.lastCommand = "AttackUnit";
            f.target = n;
            f.targetPosition = n.position;
        }
    }

    /**
     * Static pack: first contact sticks, no re-targeting. Whoever steps
     * in gets chewed; the pack never re-forms on a new victim.
     */
    public static void standAndTrade(List<FakeUnit> foes, List<FakeUnit> ours) {
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            if (f.target != null && f.target.hp > 0) {
                f.lastCommand = "AttackUnit";
                continue;
            }
            FakeUnit n = nearest(f, ours);
            f.lastCommand = "AttackUnit";
            f.target = n;
            if (n != null) f.targetPosition = n.position;
        }
    }

    /**
     * Pack focus: every zombie answers the lowest-hp live unit.
     */
    public static void focusWeakest(List<FakeUnit> foes, List<FakeUnit> ours) {
        FakeUnit weak = null;
        for (FakeUnit u : ours) {
            if (u.hp <= 0) continue;
            if (weak == null || (u.hp + u.shields) < (weak.hp + weak.shields)) weak = u;
        }
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            if (weak == null) {
                f.lastCommand = "Stop";
                continue;
            }
            f.lastCommand = "AttackUnit";
            f.target = weak;
            f.targetPosition = weak.position;
        }
    }

    /**
     * Ranged kiter: inside hold range it walks away (no shot that
     * tick), outside it stands and trades. Alternating move/attack
     * ticks are the honest zombie version of kiting - the sim resolves
     * at most one of them per tick, like the engine.
     */
    public static void kiteBack(List<FakeUnit> foes, List<FakeUnit> ours, double holdTiles) {
        for (FakeUnit f : foes) {
            if (f.hp <= 0) continue;
            FakeUnit n = nearest(f, ours);
            if (n == null) {
                f.lastCommand = "Stop";
                continue;
            }
            if (f.distTo(n) < holdTiles) {
                double dx = f.x() - n.x();
                double dy = f.y() - n.y();
                double len = Math.max(1, Math.hypot(dx, dy));
                double step = 3 * 32.0;
                f.targetPosition = atlantis.map.position.APosition.createFromPixels(
                    (int) (f.x() + dx / len * step), (int) (f.y() + dy / len * step));
                f.lastCommand = "Move";
            }
            else {
                f.lastCommand = "AttackUnit";
                f.target = n;
                f.targetPosition = n.position;
            }
        }
    }

    /**
     * Inert control: no orders, no retaliation - tests our targeting alone.
     */
    public static void holdStill(List<FakeUnit> foes, List<FakeUnit> ours) {
        for (FakeUnit f : foes) {
            f.lastCommand = "Stop";
            f.target = null;
        }
    }

    private static FakeUnit nearest(FakeUnit f, List<FakeUnit> ours) {
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

        return n;
    }
}
