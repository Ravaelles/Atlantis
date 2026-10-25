package atlantis.combat.formation;

import atlantis.game.A;
import atlantis.map.position.APosition;
import atlantis.units.AUnit;
import atlantis.units.select.Selection;

/**
 * Shared escape direction (formation Phase A, step 1 of the adoption
 * order): the leader picks one threat-gradient direction per ~10 frames
 * and every follower steps along it, instead of each unit picking its
 * own compass point and the line breaking. Followers still step from
 * their own positions (no teleporting to the leader), only the
 * DIRECTION is shared - the shape of the retreat stays a formation
 * instead of a scatter.
 *
 * <p>Fallback chain per step: shared-direction candidate (walkable) →
 * the caller's per-unit sampling (unchanged) → fight in place. A null
 * return here never strands a unit.</p>
 *
 * <p>Fewer than three combat units means no sharing - duels and pairs
 * keep the frozen per-unit behavior byte-identical.</p>
 */
public final class SharedEscape {

    /** Leader re-picks the direction at most this often (frames). */
    public static final int REDIRECTION_EVERY_FRAMES = 10;

    private static int cachedBucket = -1;
    private static int cachedLeaderId = -1;
    private static double cachedDx = 0;
    private static double cachedDy = 0;
    private static boolean cachedValid = false;

    private SharedEscape() {
    }

    /**
     * Rally point for a fleeing unit along the cluster's shared escape
     * direction, or null when sharing does not apply (too few units, no
     * leader, no threat on the leader, candidate unwalkable) - the caller
     * then falls back to the per-unit sampling, unchanged.
     *
     * @param rallyTiles how far along the direction to rally (tiles)
     * @param threatTiles threat envelope the leader reads (tiles)
     */
    public static APosition rallyFor(AUnit unit, AUnit leader, Selection cluster,
                                     double rallyTiles, double threatTiles) {
        if (unit == null || !unit.isAlive()) return null;
        if (leader == null || !leader.isAlive()) return null;
        if (cluster == null || !FormationSlots.applies(cluster.count())) return null;

        AUnit threat = atlantis.combat.targeting.ASquadTargeting
            .nearestEngageableEnemy(leader, threatTiles);
        if (threat == null) return null;

        int bucket = A.now() / REDIRECTION_EVERY_FRAMES;
        if (!cachedValid || bucket != cachedBucket
            || cachedLeaderId != leader.id()
            || leader.distTo(threat) < 0.5) {
            recompute(leader, threat, bucket);
        }
        if (!cachedValid) return null;

        APosition dest = unit.position().translateByTiles(
            cachedDx * rallyTiles, cachedDy * rallyTiles);
        if (dest == null) return null;
        if (unit.isGroundUnit() && !dest.isWalkable()) return null;

        return dest;
    }

    public static void clearForTests() {
        cachedBucket = -1;
        cachedLeaderId = -1;
        cachedValid = false;
    }

    // =========================================================

    private static void recompute(AUnit leader, AUnit threat, int bucket) {
        double dx = leader.tx() - threat.tx();
        double dy = leader.ty() - threat.ty();
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5) {
            cachedValid = false;

            return;
        }
        cachedDx = dx / len;
        cachedDy = dy / len;
        cachedBucket = bucket;
        cachedLeaderId = leader.id();
        cachedValid = true;
    }
}
