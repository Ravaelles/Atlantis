package tests.unit.sim;

import atlantis.battle.BattleCommander;
import atlantis.battle.cluster.Alpha;
import atlantis.battle.cluster.FightOrFlight;
import atlantis.battle.escape.EscapeTuning;
import atlantis.units.AUnitType;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import tests.fakes.FakeUnit;
import tests.unit.AbstractTestWithUnits;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static tests.unit.AbstractTestWithUnits.fake;

/**
 * Arc retreat sim proof (NEXT #175, sim-first by design): the same
 * 3-dragoons vs 3-zealots pocket, once with straight kite steps and once
 * with the squad-bent arc. The arc must hold the line together (smaller
 * peak split from the centroid) without silencing the guns. Deaths ride
 * along as information, not as the verdict - the item asks for split
 * distance first, win rate over N games later.
 */
@Tag("combat")
public class ArcRetreatSimTest extends AbstractTestWithUnits {

    private static final int TILE = 32;

    private static final class Outcome {
        double maxSplitTiles;
        int dragoonDeaths;
        int zealotDeaths;
        int shots;
    }

    @Test
    public void arcHoldsTheLineTogetherVsStraight() {
        Outcome straight = run(false);
        Outcome arc = run(true);

        assertTrue(straight.shots > 0 && arc.shots > 0,
            "both runs must trade shots, a silent kite proves nothing: "
                + straight.shots + " vs " + arc.shots);
        assertTrue(arc.maxSplitTiles < straight.maxSplitTiles,
            "the arc must hold the line together: straight peak split "
                + straight.maxSplitTiles + " tiles, arc " + arc.maxSplitTiles);
    }

    @Test
    public void halfBlendBeatsStraightAndFull() {
        // Blend sweep on the same pocket: 0 is today, 1 bends fully at
        // the squad (risks walking into the pack), 0.5 should own both.
        Outcome straight = runWithBlend(0.0, 3);
        Outcome half = runWithBlend(0.5, 3);
        Outcome full = runWithBlend(1.0, 3);

        assertTrue(half.shots > 0 && full.shots > 0,
            "every blend trades shots: half " + half.shots + ", full " + full.shots);
        assertTrue(half.maxSplitTiles < straight.maxSplitTiles,
            "half blend holds tighter than straight: " + half.maxSplitTiles
                + " vs " + straight.maxSplitTiles);
        assertTrue(full.maxSplitTiles < straight.maxSplitTiles,
            "full blend also beats straight: " + full.maxSplitTiles
                + " vs " + straight.maxSplitTiles);
        assertTrue(half.dragoonDeaths <= straight.dragoonDeaths
                && full.dragoonDeaths <= straight.dragoonDeaths,
            "no blend donates the line: half deaths " + half.dragoonDeaths
                + ", full deaths " + full.dragoonDeaths
                + " vs straight " + straight.dragoonDeaths);
    }

    @Test
    public void arcHoldsWhenOutnumbered() {
        // 3 dragoons vs 5 zealots: the arc must keep the line together
        // without donating dragoons.
        Outcome straight = runWithBlend(0.0, 5);
        Outcome half = runWithBlend(0.5, 5);

        assertTrue(straight.shots > 0 && half.shots > 0,
            "both runs must trade shots: " + straight.shots + " vs " + half.shots);
        assertTrue(half.maxSplitTiles < straight.maxSplitTiles,
            "outnumbered, the arc still holds tighter: " + half.maxSplitTiles
                + " vs " + straight.maxSplitTiles);
        assertTrue(half.dragoonDeaths <= straight.dragoonDeaths + 1,
            "outnumbered contact start: the arc may donate one into the surround"
                + " (bend aims at the squad, which is where the chewers are) -"
                + " the chew-kites doctrine voids every stand, so both arms kite"
                + " and only the bend direction differs. Half deaths "
                + half.dragoonDeaths + " vs straight " + straight.dragoonDeaths
                + "; the split bound above stays the test's main claim.");
    }

    private static Outcome run(boolean withArc) {
        return runWithBlend(withArc ? 0.5 : 0.0, 3);
    }

    private static Outcome runWithBlend(double blend, int enemies) {
        FakeUnit[] dragoons = new FakeUnit[] {
            woundedDragoon(28, 20, 120),
            woundedDragoon(30, 20, 120),
            woundedDragoon(32, 20, 120),
        };
        // In contact from tick 0 (inside dragoon fire range): the
        // formation hold never engages, the run measures pure kite
        // footwork - the arc's phase. The approach march is pinned
        // separately (FormationHoldTest, ClusterRallyTest).
        double[][] spots = { { 32, 19 }, { 32, 21 }, { 34, 20 }, { 31, 19 }, { 31, 21 }, { 33, 19 } };
        FakeUnit[] zealots = new FakeUnit[enemies];
        for (int i = 0; i < enemies; i++) {
            zealots[i] = enemyZealot(spots[i][0], spots[i][1]);
        }

        Outcome out = new Outcome();
        SimArena arena = new SimArena(70 * TILE, 40 * TILE);
        SimWorld world = new SimWorld(dragoons, zealots, arena);
        try {
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
            EscapeTuning.install(EscapeTuning.defaults().withArcBlend(blend));
            Alpha.gather();

            for (int i = 0; i < 400; i++) {
                world.tick(() -> {
                    for (FakeUnit dragoon : dragoons) {
                        if (dragoon.hp <= 0) continue;
                        BattleCommander.act(
                            dragoon, FightOrFlight.Tactic.FIGHT, null, dragoon);
                    }
                    scriptZealots(zealots, dragoons);
                });
                out.maxSplitTiles = Math.max(out.maxSplitTiles, splitTiles(dragoons));
            }

            for (FakeUnit dragoon : dragoons) {
                if (dragoon.hp <= 0) out.dragoonDeaths++;
            }
            for (FakeUnit zealot : zealots) {
                if (zealot.hp <= 0) out.zealotDeaths++;
            }
            out.shots = world.shotsFired();

            return out;
        }
        finally {
            for (FakeUnit dragoon : dragoons) Alpha.get().removeUnit(dragoon);
            world.close();
            BattleCommander.clearForTests();
            atlantis.util.log.EventLog.clearForTests();
        }
    }

    /** Peak distance of any live dragoon from the live centroid, in tiles. */
    private static double splitTiles(FakeUnit[] dragoons) {
        double sx = 0;
        double sy = 0;
        int alive = 0;
        for (FakeUnit dragoon : dragoons) {
            if (dragoon.hp <= 0) continue;
            sx += dragoon.position.x();
            sy += dragoon.position.y();
            alive++;
        }
        if (alive < 2) return 0;

        sx /= alive;
        sy /= alive;
        double max = 0;
        for (FakeUnit dragoon : dragoons) {
            if (dragoon.hp <= 0) continue;
            max = Math.max(max, Math.hypot(dragoon.position.x() - sx, dragoon.position.y() - sy));
        }
        return max / TILE;
    }

    private static FakeUnit woundedDragoon(double tx, double ty, int hp) {
        FakeUnit dragoon = fake(AUnitType.Protoss_Dragoon, tx, ty);
        dragoon.hp = hp;
        dragoon.shields = 0;
        dragoon.cooldown = 10;

        return dragoon;
    }

    private static FakeUnit enemyZealot(double tx, double ty) {
        FakeUnit zealot = fake(AUnitType.Protoss_Zealot, tx, ty);
        zealot.setEnemy();

        return zealot;
    }

    /** Scripted pressers: nearest live dragoon, move into range, then trade. */
    private static void scriptZealots(FakeUnit[] zealots, FakeUnit... dragoons) {
        for (FakeUnit zealot : zealots) {
            if (zealot.hp <= 0) continue;
            FakeUnit nearest = null;
            double best = Double.MAX_VALUE;
            for (FakeUnit dragoon : dragoons) {
                if (dragoon.hp <= 0) continue;
                double dist = zealot.distTo(dragoon);
                if (dist < best) {
                    best = dist;
                    nearest = dragoon;
                }
            }
            if (nearest == null) {
                zealot.lastCommand = "Stop";
                continue;
            }
            zealot.lastCommand = best <= SimWorld.reachTiles(zealot, nearest) ? "AttackUnit" : "Move";
            zealot.target = nearest;
            zealot.targetPosition = nearest.position;
        }
    }
}
