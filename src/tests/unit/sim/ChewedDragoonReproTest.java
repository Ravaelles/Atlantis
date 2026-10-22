package tests.unit.sim;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.units.AUnitType;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Reproduction scenario (owner order): 4 dragoons + 3 zealots vs 8 enemy
 * zealots, ~10 tiles apart. Live (game 185100) a dragoon stood ~300
 * frames with zealots on top, took 6-8 hits without ever moving, and
 * died - kiting must have absolute precedence, even one second chewed
 * is unacceptable. This scenario runs the REAL decision code
 * (BattleCommander.act) in the sim and reports whether any enemy
 * zealot reaches hitting range of a dragoon and for how long.
 *
 * <p>Diagnostic only for now: it prints the chew timeline so the fix
 * can be measured against it. No pass/fail on the bug itself yet.</p>
 */
@Tag("combat")
public class ChewedDragoonReproTest extends AbstractTestWithUnits {
    private static final int TILE = 32;

    /** Inside this the dragoon is being hit, per the sim's edge-to-edge reach. */
    private static final double CHEW_TILES = 0.5;

    @Test
    public void zealotsMustNeverReachDragoons() {
        FakeUnit[] dragoons = new FakeUnit[] {
            mk(20, 19), mk(21, 20), mk(22, 20), mk(23, 19),
        };
        FakeUnit[] zealots = new FakeUnit[] {
            mkz(20, 22), mkz(21, 22), mkz(22, 22),
        };
        FakeUnit[] foes = new FakeUnit[] {
            me(30, 18), me(31, 19), me(30, 20), me(31, 21),
            me(30, 22), me(31, 23), me(32, 20), me(32, 21),
        };
        List<FakeUnit> ours = new ArrayList<FakeUnit>();
        for (FakeUnit u : dragoons) ours.add(u);
        for (FakeUnit u : zealots) ours.add(u);

        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(
            ours.toArray(new FakeUnit[0]), foes, arena);

        int[] chewedFrames = new int[] { 0 };
        int[] maxConsecutive = new int[] { 0 };
        int[] curConsecutive = new int[] { 0 };
        int[] minHp = new int[] { Integer.MAX_VALUE };
        int[] kiteTrueWhileChewed = new int[] { 0 };
        int[] kiteFalseWhileChewed = new int[] { 0 };
        int[] standSurrounded = new int[] { 0 };
        int[] standLowHp = new int[] { 0 };
        int[] standMargin = new int[] { 0 };
        int ticks = 700;
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            Alpha.gather();

            for (int i = 0; i < ticks; i++) {
                world.tick(() -> {
                    atlantis.map.position.APosition anchor =
                        atlantis.map.position.APosition.create(60, 20);
                    for (FakeUnit u : ours) {
                        if (u.hp <= 0) continue;
                        BattleCommander.act(u, FightOrFlight.Tactic.FIGHT, anchor, dragoons[0]);
                    }
                    scriptFoes(foes, ours);
                });

                boolean anyChewed = false;
                for (FakeUnit d : dragoons) {
                    if (d.hp <= 0 || d.position() == null) continue;
                    int cur = d.hp + d.shields;
                    if (cur < minHp[0]) minHp[0] = cur;
                    for (FakeUnit f : foes) {
                        if (f.hp <= 0 || f.position() == null) continue;
                        if (d.distTo(f) <= CHEW_TILES) {
                            anyChewed = true;
                            break;
                        }
                    }
                    if (anyChewed) break;
                }                if (anyChewed) {
                    chewedFrames[0]++;
                    curConsecutive[0]++;
                    if (curConsecutive[0] > maxConsecutive[0]) {
                        maxConsecutive[0] = curConsecutive[0];
                    }
                    for (FakeUnit d : dragoons) {
                        if (d.hp <= 0 || d.position() == null) continue;
                        boolean chewed = false;
                        for (FakeUnit f : foes) {
                            if (f.hp <= 0 || f.position() == null) continue;
                            if (d.distTo(f) <= CHEW_TILES) {
                                chewed = true;
                                break;
                            }
                        }
                        if (!chewed) continue;
                        try {
                            atlantis.units.AUnit threat = atlantis.battle.targeting.ASquadTargeting
                                .nearestEngageableEnemy(d, atlantis.battle.kite.CombatTuning
                                    .current().getKiteEnemyTiles());
                            if (threat != null
                                && atlantis.battle.kite.Kite.shouldKite(d, threat)) {
                                kiteTrueWhileChewed[0]++;
                            }
                            else {
                                kiteFalseWhileChewed[0]++;
                                classifyStand(d, threat, standSurrounded,
                                    standLowHp, standMargin);
                            }
                        } catch (Exception e) {
                            kiteFalseWhileChewed[0]++;
                        }
                    }
                }
                else {
                    curConsecutive[0] = 0;
                }

                boolean allDead = true;
                for (FakeUnit u : ours) if (u.hp > 0) allDead = false;
                if (allDead) break;
            }

            StringBuilder per = new StringBuilder();
            for (FakeUnit d : dragoons) {
                per.append("dragoon#").append(d.id())
                    .append(" hp=").append(d.hp <= 0 ? 0 : d.hp + d.shields)
                    .append(" stuck=").append(world.stuckFrames(d)).append("; ");
            }
            System.err.println("REPRO ticks=" + world.frame()
                + " chewedFrames=" + chewedFrames[0]
                + " maxConsecutiveChewed=" + maxConsecutive[0]
                + " minDragoonHp=" + (minHp[0] == Integer.MAX_VALUE ? "n/a" : minHp[0])
                + " oursAlive=" + world.aliveOurs()
                + " foesAlive=" + world.aliveEnemies()
                + " ourShots=" + ourShots(world)
                + " kiteTrueWhileChewed=" + kiteTrueWhileChewed[0]
                + " kiteFalseWhileChewed=" + kiteFalseWhileChewed[0]
                + " stand[surrounded=" + standSurrounded[0]
                + " lowHp=" + standLowHp[0]
                + " margin=" + standMargin[0] + "] | " + per);
            System.err.println("REPRO HOLD/KITE timeline: " + holdKiteTimeline(dragoons));

            org.junit.jupiter.api.Assertions.assertEquals(0, kiteFalseWhileChewed[0],
                "owner doctrine: a chewed dragoon ALWAYS wants to kite - no stand rule"
                + " (screen, surrounded, low-hp) may hold it. " + kiteFalseWhileChewed[0]
                + " chewed frames stood by doctrine, max consecutive chew "
                + maxConsecutive[0] + " frames.");
        }
        finally {
            for (FakeUnit u : ours) Alpha.get().removeUnit(u);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    /**
     * Names which stand doctrine owned a chewed-but-not-kiting frame:
     * the surrounded pocket rule, the meat-screen rule, the low-hp
     * fire discipline, or the plain margin gate.
     */
    private static void classifyStand(atlantis.units.AUnit d,
        atlantis.units.AUnit threat, int[] surrounded,
        int[] lowHp, int[] margin) {
        try {
            if (d.hp() + d.shields() < 45
                && d.cooldown() <= atlantis.battle.kite.CombatTuning.current()
                    .getDragoonLowHpFireCooldown()) {
                lowHp[0]++;

                return;
            }
            int near = 0;
            try {
                near = d.enemiesNear().melee().inRadius(3, d).size();
            } catch (Exception ignored) {
            }
            if (near >= 3) {
                surrounded[0]++;

                return;
            }
            margin[0]++;
        } catch (Exception e) {
            margin[0]++;
        }
    }

    private static String holdKiteTimeline(FakeUnit[] dragoons) {        java.util.Set<Integer> ids = new java.util.HashSet<Integer>();
        for (FakeUnit d : dragoons) ids.add(d.id());
        StringBuilder sb = new StringBuilder();
        try {
            String all = atlantis.util.log.EventLog.toJsonLines();
            for (String line : all.split("\n")) {
                if (!line.contains("\"key\":\"HOLD\"") && !line.contains("\"key\":\"KITE\"")) continue;
                boolean mine = false;
                for (Integer id : ids) {
                    if (line.contains("\"unit\":" + id)) {
                        mine = true;
                        break;
                    }
                }
                if (mine) sb.append(line).append("\n");
            }
        } catch (Exception e) {
            sb.append("timeline-read-failed");
        }

        return sb.toString();
    }

    private static int ourShots(SimWorld world) {        try {
            java.lang.reflect.Field f = SimWorld.class.getDeclaredField("ourShotsFired");
            f.setAccessible(true);

            return (Integer) f.get(world);
        } catch (Exception e) {
            return -1;
        }
    }

    private static FakeUnit mk(double tx, double ty) {
        FakeUnit d = fake(AUnitType.Protoss_Dragoon, tx, ty);
        d.hp = 180;
        d.shields = 80;
        d.cooldown = 0;

        return d;
    }

    private static FakeUnit mkz(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.hp = 160;
        z.shields = 60;
        z.cooldown = 0;

        return z;
    }

    private static FakeUnit me(double tx, double ty) {
        FakeUnit z = fake(AUnitType.Protoss_Zealot, tx, ty);
        z.setEnemy();
        z.hp = 160;
        z.shields = 60;
        z.cooldown = 0;

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
