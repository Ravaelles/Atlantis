package atlantis.combat.escape;

import atlantis.map.position.APosition;

/**
 * Arc retreat (NEXT #175, owner 2026-10-21): a Dragoon costs ~two Zealots,
 * so kiting straight away from faster enemies weakens the army - fleeing
 * splits the line apart (strategic cost outweighing the micro gain). While
 * within squad range, bend the straight escape step back toward the squad
 * center instead of running straight away: stay in firing range of the
 * pack, keep focus fire on the weakest enemy.
 *
 * <p>Pure geometry, no game meaning inside: the caller owns the radius
 * gate and the blend weight (both live in {@link EscapeTuning}, default
 * zero = bit-identical straight steps). Bend rotates the away-vector
 * toward the squad vector by the blend fraction of the signed angle,
 * keeping the step length - a blend of 1 walks at the squad, 0 walks
 * straight away, and anything between arcs back toward the line.</p>
 */
public final class EscapeArc {

    /**
     * Beyond this the squad pull stops: far units flee straight, never across the map.
     */
    public static final double SQUAD_RADIUS_TILES = 15.0;

    private EscapeArc() {
    }

    /**
     * Bend the straight escape step toward the squad center. Null-safe:
     * any missing point, a zero-length step, or a non-positive blend
     * returns the straight destination unchanged (blend 0 IS today's
     * behavior, exactly).
     */
    public static APosition bend(
        APosition from, APosition straightDest, APosition squadCenter, double blend
    ) {
        if (from == null || straightDest == null || squadCenter == null) return straightDest;
        if (!(blend > 0)) return straightDest;

        if (blend > 1) blend = 1;
        double ax = straightDest.x() - from.x();
        double ay = straightDest.y() - from.y();
        double len = Math.hypot(ax, ay);
        if (len <= 0) return straightDest;

        double sx = squadCenter.x() - from.x();
        double sy = squadCenter.y() - from.y();
        if (Math.hypot(sx, sy) <= 0) return straightDest;

        double angledAway = Math.atan2(ay, ax);
        double angledSquad = Math.atan2(sy, sx);
        double delta = angledSquad - angledAway;
        while (delta > Math.PI) delta -= 2 * Math.PI;
        while (delta < -Math.PI) delta += 2 * Math.PI;
        double angled = angledAway + blend * delta;
        return APosition.createFromPixels(
            (int) (from.x() + len * Math.cos(angled)),
            (int) (from.y() + len * Math.sin(angled)));
    }
}
