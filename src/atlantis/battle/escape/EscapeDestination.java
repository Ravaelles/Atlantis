package atlantis.battle.escape;

import atlantis.battle.kite.KiteTwin;
import atlantis.map.position.APosition;
import atlantis.map.position.HasPosition;
import atlantis.units.AUnit;

import java.util.List;

/**
 * Where to step when the duel kiting yields ground: the 8 compass points
 * at exactly the needed length, filtered by walkability and body width,
 * then scored (static obstacles at the destination, corner clearance
 * around it, enemy pressure on it). Tie-break: farther from the threat
 * wins. Null when no direction steps - the caller keeps its own fallback.
 */
public final class EscapeDestination {

    private EscapeDestination() {
    }

    public static APosition pick(
        AUnit unit, AUnit threat, int stepTiles,
        EscapeRays.Walkability walk, List<? extends HasPosition> blockers, List<? extends HasPosition> enemies
    ) {
        if (unit == null || threat == null || stepTiles <= 0 || walk == null) return null;
        boolean wide = unit.type() != null && unit.type().isLarge();
        int ux = unit.x() / 32;
        int uy = unit.y() / 32;
        APosition best = null;
        double bestScore = Double.MAX_VALUE;
        double bestDist = -1;
        for (int[] dir : EscapeRays.DIRS) {
            int tx = ux + dir[0] * stepTiles;
            int ty = uy + dir[1] * stepTiles;
            if (tx < 0 || ty < 0 || !walk.isWalkable(tx, ty)) continue;
            if (!corridorClear(ux, uy, dir, stepTiles, wide, walk)) continue;
            APosition dest = APosition.create(tx, ty);
            double score = EscapeScoring.staticPenalty(
                    dest, blockers, EscapeTuning.current().getStaticRadiusTiles())
                + EscapeScoring.cornerPenalty(EscapeRays.clearance(dest, 6, walk))
                + EscapeScoring.enemyPressure(
                    dest, enemies, EscapeTuning.current().getEnemyRadiusTiles());
            double dist = dest.distTo(threat);
            if (score < bestScore - 0.001 || (Math.abs(score - bestScore) <= 0.001 && dist > bestDist)) {
                bestScore = score;
                bestDist = dist;
                best = dest;
            }
        }
        return best;
    }

    /**
     * Full-length escape step (NEXT #173, owner doctrine): walk straight
     * out as far as possible - 5 tiles. Only when the straight-5 is
     * blocked does the horizon shrink: deflect at full length first,
     * shorten only as the last resort. Shortening first is what re-traps
     * units (a 1-tile step into a pocket is still a step, so the caller
     * keeps taking it instead of sliding along the wall).
     */
    public static final int LONG_STEP_TILES = 5;

    /**
     * Detector-first escape pick (NEXT #173): the tactical step when the
     * ground ahead is open, the full-length deflect when a wall eats the
     * straight ray, the boxed step (or null) when nothing longer exists.
     * Open ground answers bit-identically to {@link #pick} with the same
     * length; only the wall case changes (sidestep instead of shuffle).
     * Callers keep their null fallback - a true box still fights.
     */
    public static APosition pickEscape(
        AUnit unit, AUnit threat, int stepTiles,
        EscapeRays.Walkability walk, List<? extends HasPosition> blockers, List<? extends HasPosition> enemies
    ) {
        if (unit == null || threat == null || stepTiles <= 0 || walk == null) return null;
        APosition shortPick = pick(unit, threat, stepTiles, walk, blockers, enemies);
        int away = EscapeRays.awayDirIndex(unit, threat);
        double[] clear = EscapeRays.clearance(unit.position(), LONG_STEP_TILES, walk);
        if (shortPick != null
            && (stepTiles >= LONG_STEP_TILES || clear[away] >= LONG_STEP_TILES)) {
            return shortPick;
        }
        if (LONG_STEP_TILES != stepTiles) {
            APosition longPick = pick(unit, threat, LONG_STEP_TILES, walk, blockers, enemies);
            if (longPick != null) {
                try {
                    KiteTwin.longEscape(unit, threat);
                } catch (Exception | Error ignored) {
                    // Diagnostics never break the decision.
                }
                return longPick;
            }
        }
        return shortPick;
    }

    /** Every tile on the way must be walkable; wide bodies need one spare past the step. */
    static boolean corridorClear(int ux, int uy, int[] dir, int stepTiles, boolean wide, EscapeRays.Walkability walk) {
        int look = stepTiles + (wide ? 1 : 0);
        for (int i = 1; i <= look; i++) {
            if (!walk.isWalkable(ux + dir[0] * i, uy + dir[1] * i)) return false;
        }
        return true;
    }
}
