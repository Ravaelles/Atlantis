package atlantis.battle.formation;

import atlantis.battle.kite.CombatTuning;
import atlantis.battle.targeting.ASquadTargeting;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Select;
import atlantis.units.select.Selection;

/**
 * Covering withdrawal (formation Phase A, NEXT #162): the wounded-first-rank
 * case. A pinch-yielding Dragoon publishes its escape ray by moving; a
 * healthy unit standing in that corridor steps laterally one step and
 * resumes; a healthy unit with no personal threat covers the pursuer
 * instead of its nearest.
 *
 * <p>Reuses the pinch semantics exactly (sub-pinch-hp Dragoon pressed by
 * melee yields ground): no new knobs, no new tunables. Gated to 3+ army
 * units like every Phase A piece, so duels and pairs keep the frozen
 * per-unit behavior byte-identical. Both entry points answer null when
 * the job is someone else's - the caller falls through unchanged.
 */
public final class CoverWithdraw {

    /** Escape ray length the corridor covers, pixels. */
    private static final double RAY_PX = 64.0;
    /** Corridor half-width, pixels: only the ray itself, not the area. */
    private static final double HALF_WIDTH_PX = 16.0;
    /** Only an escaper this close gets a unit interrupted, pixels. */
    private static final double URGENT_PX = 48.0;
    /** Lateral step length, pixels (one tile). */
    private static final double SIDESTEP_PX = 32.0;

    private CoverWithdraw() {
    }

    /** A Dragoon yielding ground under melee pressure: the pinched unit. */
    public static boolean isWithdrawing(AUnit unit) {
        if (unit == null || !unit.isAlive() || !unit.isDragoon()) return false;
        if (unit.hp() + unit.shields() >= CombatTuning.current().getDragoonPinchHp()) return false;
        return unit.enemiesNear().melee().inRadius(
            CombatTuning.current().getDragoonPinchMeleeTiles(), unit).size() >= 1;
    }

    /**
     * Lateral step clearing the escaper's corridor, or null (no withdrawing
     * ally, not blocking, duel-scale cluster, no walkable sidestep - the
     * caller fights on unchanged).
     */
    public static APosition makeWayStep(AUnit helper) {
        if (helper == null || !helper.isAlive()) return null;
        if (isWithdrawing(helper)) return null;
        Selection cluster = Select.ourCombatUnits();
        if (cluster == null || !FormationSlots.applies(cluster.count())) return null;
        for (AUnit ally : cluster.list()) {
            if (ally == null || ally.id() == helper.id() || !isWithdrawing(ally)) continue;
            AUnit threat = nearestMeleeThreat(ally);
            if (threat == null) continue;
            if (!blocksCorridor(helper, ally, threat)) continue;
            APosition step = lateralStep(helper, ally, threat);
            if (step != null) return step;
        }
        return null;
    }

    /**
     * The pursuer to cover when healthy has nothing urgent of its own:
     * null when committed (sticky target handled by the caller - this
     * only fills the uncommitted gap), when an enemy presses THIS unit
     * inside 2 tiles (immediate danger beats covering - deliberately
     * tighter than the kite envelope: a pursuer at 3-4 tiles sits in
     * our weapon range without being our emergency), when nobody
     * withdraws, or when the pursuer is out of our reach/invalid.
     */
    public static AUnit coverTarget(AUnit healthy) {
        if (healthy == null || !healthy.isAlive()) return null;
        Selection cluster = Select.ourCombatUnits();
        if (cluster == null || !FormationSlots.applies(cluster.count())) return null;
        AUnit urgent = ASquadTargeting.nearestEngageableEnemy(healthy, 2.0);
        if (urgent != null) return null;
        for (AUnit ally : cluster.list()) {
            if (ally == null || ally.id() == healthy.id() || !isWithdrawing(ally)) continue;
            AUnit pursuer = nearestMeleeThreat(ally);
            if (pursuer == null || !pursuer.isAlive()) continue;
            if (!healthy.hasWeaponToAttackThisUnit(pursuer)) continue;
            // Pursue-validity, deliberately NOT the focus gate above:
            // focus optimizes DPS (lings, workers and the wounded stay
            // out), cover answers "who is killing the withdrawer" - the
            // usual answer is a ling, which focus never picks. Shared
            // exclusions stay shared: non-combat buildings and dead men
            // (in-flight damage already spoken for) are never covered.
            if (pursuer.isABuilding() && !pursuer.isCombatBuilding()) continue;
            if (pursuer.isDeadMan()) continue;
            if (healthy.distTo(pursuer) > healthy.groundWeaponRange()) continue;
            return pursuer;
        }
        return null;
    }

    // =========================================================

    private static AUnit nearestMeleeThreat(AUnit unit) {
        Selection melee = unit.enemiesNear().melee();
        if (melee == null) return null;
        AUnit nearest = null;
        double best = Double.MAX_VALUE;
        for (AUnit enemy : melee.list()) {
            if (enemy == null || !enemy.isAlive()) continue;
            double dist = unit.distTo(enemy);
            if (dist < best) {
                best = dist;
                nearest = enemy;
            }
        }
        return nearest;
    }

    static boolean blocksCorridor(AUnit helper, AUnit escaper, AUnit threat) {
        double ex = escaper.position().x();
        double ey = escaper.position().y();
        double dx = ex - threat.position().x();
        double dy = ey - threat.position().y();
        double len = Math.hypot(dx, dy);
        if (len < 1.0) return false;
        double ux = dx / len;
        double uy = dy / len;
        double rx = helper.position().x() - ex;
        double ry = helper.position().y() - ey;
        double along = rx * ux + ry * uy;
        if (along <= 0 || along >= RAY_PX) return false;
        double perp = Math.abs(rx * uy - ry * ux);
        if (perp >= HALF_WIDTH_PX) return false;
        return Math.hypot(rx, ry) < URGENT_PX;
    }

    private static APosition lateralStep(AUnit helper, AUnit escaper, AUnit threat) {
        double ex = escaper.position().x();
        double ey = escaper.position().y();
        double dx = ex - threat.position().x();
        double dy = ey - threat.position().y();
        double len = Math.hypot(dx, dy);
        if (len < 1.0) return null;
        double ux = dx / len;
        double uy = dy / len;
        double hx = helper.position().x();
        double hy = helper.position().y();
        APosition first = stepOrNull(helper, hx - uy * SIDESTEP_PX, hy + ux * SIDESTEP_PX, threat);
        APosition second = stepOrNull(helper, hx + uy * SIDESTEP_PX, hy - ux * SIDESTEP_PX, threat);
        if (first == null) return second;
        if (second == null) return first;
        return distPx(first, threat) >= distPx(second, threat) ? first : second;
    }

    private static APosition stepOrNull(AUnit helper, double x, double y, AUnit threat) {
        APosition dest = helper.position().translateByPixels(
            (int) Math.round(x - helper.position().x()),
            (int) Math.round(y - helper.position().y()));
        if (dest == null) return null;
        if (helper.isGroundUnit() && !dest.isWalkable()) return null;
        if (distPx(dest, threat) < distPx(helper.position(), threat)) return null;
        return dest;
    }

    private static double distPx(APosition a, AUnit b) {
        double dx = a.x() - b.position().x();
        double dy = a.y() - b.position().y();
        return Math.hypot(dx, dy);
    }
}
