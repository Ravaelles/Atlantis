package atlantis.battle.escape;

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
            double score = EscapeScoring.staticPenalty(dest, blockers, EscapeScoring.STATIC_RADIUS_TILES)
                + EscapeScoring.cornerPenalty(EscapeRays.clearance(dest, 6, walk))
                + EscapeScoring.enemyPressure(dest, enemies, EscapeScoring.ENEMY_RADIUS_TILES);
            double dist = dest.distTo(threat);
            if (score < bestScore - 0.001 || (Math.abs(score - bestScore) <= 0.001 && dist > bestDist)) {
                bestScore = score;
                bestDist = dist;
                best = dest;
            }
        }
        return best;
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
