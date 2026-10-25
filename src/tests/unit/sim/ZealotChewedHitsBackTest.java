package tests.unit.sim;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.combat.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.combat.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeOrderSink;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Repro of the Steamhammer loss (Zealot#177, owner report): surrounded by
 * Zerglings, no stable target for 5 seconds (tgt- while n2 grew 3 to 9),
 * marched to a fixed rally and died - while the orders show attacks on
 * DISTANT Zerglings (#164/#173/#153 churn).
 *
 * <p>Two compounding causes, both pinned here against zombie opponents
 * (chase-nearest + stand-and-trade): (1) the focus chain bans Zerglings
 * from every focus path, so keepable drops the chewer every frame and
 * the fallbacks hand far targets; (2) the FLEE march has no grabbed
 * guard, so a unit with its shot ready walks home through the pack.</p>
 *
 * <p>Rule (owner): a melee unit with its shot ready (cooldown &lt;= 5)
 * ALWAYS hits the weakest enemy IN RANGE instead of chasing far
 * targets or marching.</p>
 */
@Tag("combat")
public class ZealotChewedHitsBackTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static FakeUnit zealot(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.hp = 100;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit zling(double tx, double ty, int hp) {
        FakeUnit l = fake(AUnitType.Zerg_Zergling, tx, ty);
        l.setEnemy();
        l.hp = hp;

        return l;
    }

    private static FakeUnit[] surround(double cx, double cy) {
        // H: healthy, nearest (0.25). W: wounded (hp 5), in range (0.35).
        // Two more pressers at 3 and 5 tiles: in enemiesNear, out of reach.
        return new FakeUnit[] {
            zling(cx + 0.25, cy, 35),
            zling(cx - 0.35, cy, 5),
            zling(cx + 3, cy + 1, 35),
            zling(cx + 5, cy - 1, 35),
        };
    }

    /** Zombie policy A: chase the nearest live unit, trade in reach. */
    private static void chaseNearest(List<FakeUnit> foes, List<FakeUnit> ours) {
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
            f.lastCommand = "AttackUnit";
            f.target = n;
            f.targetPosition = n.position;
        }
    }

    /** Zombie policy B: stand the surround, trade only - no chase. */
    private static void standAndTrade(List<FakeUnit> foes, List<FakeUnit> ours) {
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
            f.lastCommand = "AttackUnit";
            f.target = n;
            if (n != null) f.targetPosition = n.position;
        }
    }

    private static List<FakeOrderSink.Record> ordersOf(FakeUnit unit) {
        return ((FakeOrderSink) unit.orderSink()).orders();
    }

    private static Set<Integer> attackTargetsWhileAlive(List<FakeOrderSink.Record> orders, FakeUnit victim) {
        Set<Integer> targets = new HashSet<>();
        for (FakeOrderSink.Record r : orders) {
            if (!r.operation.equals("attackUnit")) continue;
            if (victim.hp <= 0) break;
            try {
                targets.add(Integer.parseInt(r.detail.trim()));
            } catch (NumberFormatException ignored) {
            }
        }

        return targets;
    }

    private static Double farthestMoveDest(List<FakeOrderSink.Record> orders, FakeUnit unit) {
        double farthest = 0;
        // FakeOrderSink records move dests as "[x, y]" in PIXELS
        // (Position.toString, not APosition).
        Pattern p = Pattern.compile("\\[\\s*(\\d+)\\s*,\\s*(\\d+)\\s*\\]");
        for (FakeOrderSink.Record r : orders) {
            if (!r.operation.equals("move")) continue;
            Matcher m = p.matcher(r.detail);
            if (!m.find()) continue;
            double dx = Integer.parseInt(m.group(1)) / 32.0;
            double dy = Integer.parseInt(m.group(2)) / 32.0;
            double d = Math.hypot(dx - unit.x() / 32.0, dy - unit.y() / 32.0);
            if (d > farthest) farthest = d;
        }

        return farthest;
    }

    private void runScenario(java.util.function.BiConsumer<List<FakeUnit>, List<FakeUnit>> policy, FightOrFlight.Tactic tactic, Listener listener) {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = zealot(30, 20);
        FakeUnit[] foeArr = surround(30, 20);
        List<FakeUnit> ours = new ArrayList<>();
        ours.add(z);
        List<FakeUnit> foes = new ArrayList<>();
        for (FakeUnit f : foeArr) foes.add(f);

        FakeUnit[] foeIds = foeArr;
        int weakId = foeArr[1].id();

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, z }, foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < 120; i++) {
                final int tick = i;
                world.tick(() -> {
                    if (z.hp > 0) {
                        BattleCommander.act(z, tactic, nexus.position(), z);
                    }
                    policy.accept(foes, ours);
                });
                listener.onTick(tick, z, foeIds, weakId, ordersOf(z));
                if (z.hp <= 0) break;
            }
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    private interface Listener {
        void onTick(int tick, FakeUnit z, FakeUnit[] foes, int weakId, List<FakeOrderSink.Record> orders);
    }

    @Test
    public void grabbedZealotHitsWeakestInRangeVsChasers() {
        List<Integer> firstTargets = new ArrayList<>();
        Set<Integer> targetsWhileWeakAlive = new HashSet<>();

        runScenario(ZealotChewedHitsBackTest::chaseNearest, FightOrFlight.Tactic.FIGHT, (tick, z, foes, weakId, orders) -> {
            for (FakeOrderSink.Record r : orders) {
                if (!r.operation.equals("attackUnit")) continue;
                try {
                    int id = Integer.parseInt(r.detail.trim());
                    if (firstTargets.isEmpty()) firstTargets.add(id);
                    FakeUnit weak = null;
                    for (FakeUnit f : foes) if (f.id() == weakId) weak = f;
                    if (weak != null && weak.hp > 0) targetsWhileWeakAlive.add(id);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        assertFalse(firstTargets.isEmpty(), "a grabbed zealot with its shot ready shoots, not marches");
        assertTrue(targetsWhileWeakAlive.size() == 1 && targetsWhileWeakAlive.contains(firstTargets.get(0)),
            "no churn to distant targets while the weak in-range ling lives: " + targetsWhileWeakAlive);
    }

    @Test
    public void grabbedZealotHitsWeakestInRangeVsStaticPack() {
        List<Integer> firstTargets = new ArrayList<>();
        Set<Integer> targetsWhileWeakAlive = new HashSet<>();

        runScenario(ZealotChewedHitsBackTest::standAndTrade, FightOrFlight.Tactic.FIGHT, (tick, z, foes, weakId, orders) -> {
            for (FakeOrderSink.Record r : orders) {
                if (!r.operation.equals("attackUnit")) continue;
                try {
                    int id = Integer.parseInt(r.detail.trim());
                    if (firstTargets.isEmpty()) firstTargets.add(id);
                    FakeUnit weak = null;
                    for (FakeUnit f : foes) if (f.id() == weakId) weak = f;
                    if (weak != null && weak.hp > 0) targetsWhileWeakAlive.add(id);
                } catch (NumberFormatException ignored) {
                }
            }
        });

        assertFalse(firstTargets.isEmpty(), "a grabbed zealot with its shot ready shoots, not marches");
        assertTrue(targetsWhileWeakAlive.size() == 1,
            "one stable in-range target, never the far pressers: " + targetsWhileWeakAlive);
    }

    @Test
    public void fleeingZealotDoesNotMarchWhileGrabbedAndReady() {
        FakeUnit nexus = fake(AUnitType.Protoss_Nexus, 10, 10);
        FakeUnit z = zealot(30, 20);
        FakeUnit[] foeArr = surround(30, 20);
        List<FakeUnit> foes = new ArrayList<>();
        for (FakeUnit f : foeArr) foes.add(f);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            new FakeUnit[] { nexus, z }, foes.toArray(new FakeUnit[0]), arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            z.cooldown = 0;
            world.tick(() -> BattleCommander.act(z, FightOrFlight.Tactic.FLEE, nexus.position(), z));

            double farthest = farthestMoveDest(ordersOf(z), z);
            assertTrue(farthest < 5,
                "grabbed with shot ready: fight, never march home - farthest move dest " + farthest);
            assertTrue(ordersOf(z).stream().anyMatch(r -> r.operation.equals("attackUnit")),
                "the ready shot goes into the chewer: " + ordersOf(z));
        }
        finally {
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

}
